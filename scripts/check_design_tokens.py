#!/usr/bin/env python3
"""Spec-conformance checks for the design system.

The sandbox this project was built in has no JVM, so the Kotlin sources cannot be compiled here.
These checks cover the failure modes that a compiler would otherwise catch and that are easy to get
wrong by hand:

  1. Every color token named in specs/DESIGN.md exists in the theme with the exact hex.
  2. Every `KiteSpacing.x` / `KiteSize.x` / `KiteRadius.x` / `colors.x` reference in the codebase
     resolves to a declared token (undefined-token references are compile errors).
  3. No hardcoded hex, dp, or sp leaks into :app or :feature code — CLAUDE.md requires tokens only.
  4. Braces, parentheses, and brackets balance in every Kotlin file.

Run:  python3 scripts/check_design_tokens.py
Exit: 0 when clean, 1 with a list of violations.
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DESIGN = ROOT / "specs" / "DESIGN.md"
DESIGNSYSTEM = ROOT / "core" / "designsystem" / "src" / "main" / "java"

TOKEN_OBJECTS = ("KiteSpacing", "KiteSize", "KiteRadius")

failures: list[str] = []
checked = 0


def fail(msg: str) -> None:
    failures.append(msg)


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def strip_code_noise(src: str) -> str:
    """Remove comments, string, and char literals so bracket counting is meaningful."""
    out: list[str] = []
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        nxt = src[i + 1] if i + 1 < n else ""
        if c == "/" and nxt == "/":
            while i < n and src[i] != "\n":
                i += 1
        elif c == "/" and nxt == "*":
            i += 2
            while i < n - 1 and not (src[i] == "*" and src[i + 1] == "/"):
                i += 1
            i += 2
        elif src.startswith('"""', i):
            i += 3
            while i < n - 2 and not src.startswith('"""', i):
                i += 1
            i += 3
        elif c == '"':
            i += 1
            while i < n and src[i] != '"':
                i += 2 if src[i] == "\\" else 1
            i += 1
        elif c == "'":
            i += 1
            while i < n and src[i] != "'":
                i += 2 if src[i] == "\\" else 1
            i += 1
        else:
            out.append(c)
            i += 1
    return "".join(out)


def check_brackets() -> None:
    global checked
    for path in sorted(ROOT.rglob("*.kt")):
        if ".git" in path.parts:
            continue
        checked += 1
        code = strip_code_noise(read(path))
        for open_c, close_c in (("{", "}"), ("(", ")"), ("[", "]")):
            depth = 0
            for line_no, line in enumerate(code.splitlines(), 1):
                depth += line.count(open_c) - line.count(close_c)
                if depth < 0:
                    fail(f"{path.relative_to(ROOT)}:{line_no} unbalanced '{close_c}'")
                    depth = 0
            if depth != 0:
                fail(f"{path.relative_to(ROOT)}: unbalanced '{open_c}' (depth {depth} at EOF)")


def parse_design_colors() -> dict[str, str]:
    """Token name -> hex, from the color tables in DESIGN.md."""
    tokens: dict[str, str] = {}
    for match in re.finditer(r"^\|\s*`(\w+)`\s*\|\s*`(#[0-9A-Fa-f]{6})`", read(DESIGN), re.M):
        tokens[match.group(1)] = match.group(2).upper()
    return tokens


def parse_theme_colors() -> tuple[dict[str, str], set[str]]:
    """(token -> hex, all declared AppColors property names)."""
    color_kt = read(DESIGNSYSTEM / "dev/local/ytclient/core/designsystem/ui/theme/Color.kt")
    constants = {
        m.group(1): m.group(2).upper()
        for m in re.finditer(r"internal val (\w+) = Color\(0x([0-9A-Fa-f]{8})\)", color_kt)
    }

    app_colors = read(DESIGNSYSTEM / "dev/local/ytclient/core/designsystem/ui/theme/AppColors.kt")
    props = set(re.findall(r"^\s+val (\w+): Color", app_colors, re.M))

    assignment_block = app_colors.split("val KiteColors: AppColors = AppColors(", 1)[1]
    assignment_block = assignment_block.split("\n)", 1)[0]

    resolved: dict[str, str] = {}
    for token, constant in re.findall(r"(\w+) = (\w+),", assignment_block):
        argb = constants.get(constant)
        if argb is None:
            fail(f"AppColors.kt: token '{token}' maps to unknown constant '{constant}'")
            continue
        # Compare on RGB; the alpha byte is only non-FF for the scrims DESIGN.md writes as rgba().
        resolved[token] = "#" + argb[2:]
    return resolved, props


def check_color_tokens() -> None:
    global checked
    spec = parse_design_colors()
    theme, props = parse_theme_colors()
    if not spec:
        fail("could not parse any color tokens out of specs/DESIGN.md")
        return
    for token, hex_value in sorted(spec.items()):
        checked += 1
        if token not in props:
            fail(f"DESIGN.md color token '{token}' is not a property of AppColors")
        elif token not in theme:
            fail(f"DESIGN.md color token '{token}' is declared but never assigned in KiteColors")
        elif theme[token] != hex_value:
            fail(f"color '{token}': DESIGN.md says {hex_value}, theme has {theme[token]}")


def parse_token_objects() -> dict[str, set[str]]:
    src = read(DESIGNSYSTEM / "dev/local/ytclient/core/designsystem/ui/theme/Tokens.kt")
    declared: dict[str, set[str]] = {}
    for name in TOKEN_OBJECTS:
        body = src.split(f"object {name} {{", 1)
        if len(body) < 2:
            fail(f"Tokens.kt: object {name} not found")
            declared[name] = set()
            continue
        members = set()
        depth = 1  # the split consumed the object's opening brace
        for line in body[1].splitlines():
            depth += line.count("{") - line.count("}")
            if depth <= 0:
                break
            members.update(re.findall(r"^\s*val (\w+)", line))
        declared[name] = members
    return declared


def check_token_references(declared: dict[str, set[str]]) -> None:
    global checked
    color_props = parse_theme_colors()[1]
    for path in sorted(ROOT.rglob("*.kt")):
        if ".git" in path.parts:
            continue
        src = read(path)
        rel = path.relative_to(ROOT)

        for obj, members in declared.items():
            for used in set(re.findall(rf"\b{obj}\.(\w+)", src)):
                checked += 1
                if used not in members:
                    fail(f"{rel}: {obj}.{used} is not declared in Tokens.kt")

        if "val colors = AppTheme.colors" in src:
            for used in set(re.findall(r"\bcolors\.(\w+)", src)):
                checked += 1
                if used not in color_props and used != "tagColors":
                    fail(f"{rel}: colors.{used} is not a token on AppColors")


def check_no_hardcoded_values() -> None:
    """CLAUDE.md: no hardcoded hex, dp, or sp anywhere in feature code."""
    global checked
    for module in ("app", "feature"):
        base = ROOT / module
        if not base.exists():
            continue
        for path in sorted(base.rglob("*.kt")):
            src = strip_code_noise(read(path))
            rel = path.relative_to(ROOT)
            for m in re.finditer(r"Color\(0x[0-9A-Fa-f]+\)", src):
                checked += 1
                fail(f"{rel}: hardcoded {m.group(0)} — use an AppColors token")
            for m in re.finditer(r"(?<![\w.])\d+(?:\.\d+)?\.(?:dp|sp)\b", src):
                checked += 1
                fail(f"{rel}: hardcoded '{m.group(0)}' — use KiteSpacing/KiteSize/a type token")


def check_kotlin_syntax() -> bool:
    """Parse every Kotlin file with the tree-sitter Kotlin grammar, if it is installed.

    This is a real syntax check, not a heuristic: it catches malformed annotations, unbalanced
    blocks, and misplaced modifiers. It is not a type check — no JVM is available here — so
    unresolved references are still only covered by the token checks above.

    Install with:  pip install tree-sitter tree-sitter-kotlin
    """
    global checked
    try:
        import tree_sitter_kotlin
        from tree_sitter import Language, Parser
    except ImportError:
        print("note: tree-sitter-kotlin not installed; skipping Kotlin syntax pass")
        return False

    parser = Parser(Language(tree_sitter_kotlin.language()))
    for path in sorted(ROOT.rglob("*.kt")):
        if ".git" in path.parts or ".venv" in path.parts:
            continue
        checked += 1
        tree = parser.parse(read(path).encode("utf-8"))
        if tree.root_node.has_error:
            node = first_error(tree.root_node)
            where = f":{node.start_point[0] + 1}" if node else ""
            fail(f"{path.relative_to(ROOT)}{where} Kotlin syntax error")
    return True


def first_error(node):
    """Walk to the first node the parser flagged, for a useful line number."""
    if node.is_missing or (node.has_error and all(not c.has_error for c in node.children)):
        return node
    for child in node.children:
        if child.has_error:
            found = first_error(child)
            if found is not None:
                return found
    return node if node.has_error else None


def main() -> int:
    check_brackets()
    check_kotlin_syntax()
    check_color_tokens()
    check_token_references(parse_token_objects())
    check_no_hardcoded_values()

    if failures:
        print(f"FAIL — {len(failures)} violation(s) across {checked} checks:\n")
        for line in failures:
            print(f"  - {line}")
        return 1
    print(f"OK — {checked} checks passed (color tokens, token references, hardcoded values, brackets).")
    return 0


if __name__ == "__main__":
    sys.exit(main())
