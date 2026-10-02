"""从 GitHub Releases 汇总生成 CHANGELOG.md（去掉构建信息/安装说明等样板段落）。"""

import json
import os
import re
import subprocess

REPO = "wild0408/nanxinshiguang"
OUT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "CHANGELOG.md")


def gh(*args: str) -> str:
    return subprocess.run(
        ["gh", *args], capture_output=True, text=True, encoding="utf-8", check=True
    ).stdout


releases = json.loads(
    gh(
        "release",
        "list",
        "--repo",
        REPO,
        "--limit",
        "50",
        "--json",
        "tagName,publishedAt,name",
    )
)

entries = []
for item in releases:
    tag = item["tagName"]
    detail = json.loads(
        gh("release", "view", tag, "--repo", REPO, "--json", "body,publishedAt")
    )
    body = (detail.get("body") or "").strip()
    # 去掉样板段落，只保留变更内容
    body = re.split(r"^##\s*(构建信息|安装说明)", body, maxsplit=1, flags=re.M)[0].strip()
    # 去掉"更新内容"标题，并把其余二级标题降为三级，保证版本号之下的层级统一
    body = re.sub(r"^##\s*更新内容\s*$", "", body, flags=re.M)
    body = re.sub(r"^##\s+", "### ", body, flags=re.M)
    body = re.sub(r"\n{3,}", "\n\n", body).strip()
    if not body:
        body = "（该版本未记录详细说明）"
    date = detail["publishedAt"][:10]
    entries.append((tag, date, body))

lines = [
    "# 更新日志",
    "",
    "本文件记录各版本的主要变更。完整的发布说明、构建信息与安装指引见"
    f"[GitHub Releases](https://github.com/{REPO}/releases)。",
    "",
    "> 版本号遵循 `主版本.次版本.修订号`；`versionCode` 单调递增。",
    "",
]

for tag, date, body in entries:
    lines.append(f"## {tag} — {date}")
    lines.append("")
    lines.append(body)
    lines.append("")
    lines.append(
        f"[查看该版本发布页与下载](https://github.com/{REPO}/releases/tag/{tag})"
    )
    lines.append("")
    lines.append("---")
    lines.append("")

with open(OUT, "w", encoding="utf-8") as fh:
    fh.write("\n".join(lines).rstrip() + "\n")

print("已生成", OUT)
print("版本数:", len(entries))
for tag, date, body in entries:
    print(f"  {tag:<8} {date}  {len(body):>4} 字")
