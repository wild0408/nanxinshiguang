"""从 docs/images/icon.svg 生成 Android 启动图标全套资源。

用法：
    <python> tools/generate_launcher_icons.py

依赖：本机 Chrome（无头渲染 SVG）与 Pillow。生成内容：
    app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png            旧式方形图标 48..192
    app/src/main/res/mipmap-{...}/ic_launcher_background.webp               自适应背景层 108..432
    app/src/main/res/mipmap-{...}/ic_launcher_foreground.webp               自适应前景层 108..432
    app/src/main/res/mipmap-{...}/ic_launcher_monochrome.webp               Android 13+ 主题图标层
    app/src/commonMain/composeResources/drawable/ic_launcher_foreground.png 应用内「关于」页 256
    docs/images/icon-preview*.png                                           预览图（不入 APK）

设计稿要求：
- 自适应图标的前景只有中心 72/108（约 66%）保证不被启动器遮罩裁掉，四周留白。
- 背景层会全出血铺满，因此 SVG 的渐变矩形会按画布 512x512 重绘（不保留圆角）。
- 前景层保留原设计的圆角裁剪，使轨迹在边界处被裁掉，与设计稿一致。
"""

import os
import re
import subprocess

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC_SVG = os.path.join(ROOT, "docs", "images", "icon.svg")
RES = os.path.join(ROOT, "app", "src", "main", "res")
COMPOSE = os.path.join(ROOT, "app", "src", "commonMain", "composeResources", "drawable")
DOCS = os.path.join(ROOT, "docs", "images")
WORK = os.path.join(os.environ.get("TEMP", "/tmp"), "icon_gen")
CHROME_CANDIDATES = [
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    r"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
]

LEGACY = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
ADAPTIVE = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}

# 自适应图标的图层是 108x108dp，但系统只保证中间 72dp 可见（关键内容应落在约 66dp 的安全区内）。
# 设计稿是完整 512 画布（背景+卡片+轨迹几乎铺满），若 1:1 铺进图层，卡片会撑满遮罩并被裁掉四角。
# 因此前景层与主题图标层按该系数围绕画布中心缩放，背景层仍保持全出血（它要铺满遮罩）。
# 取值依据：设计稿里轨迹环最外沿距中心约 0.43*512；按"轨迹环落在 72dp 可见半径的 90% 处"取
# 0.43 * 108 * s <= 0.9 * 36 -> s <= 0.70。这样卡片约占图层 47%（约为遮罩宽度的 70%，
# 与同门应用 拾光课程表 的比例接近），同时给遮罩比 72dp 更大的 OEM 启动器（如 HyperOS）留出余量。
FOREGROUND_SCALE = 0.70


def _scaled(inner: str, scale: float) -> str:
    """把图层内容围绕画布中心缩放。"""
    if scale == 1.0:
        return inner
    return (
        f'<g transform="translate(256 256) scale({scale}) translate(-256 -256)">'
        f"{inner}</g>"
    )



def find_chrome() -> str:
    for path in CHROME_CANDIDATES:
        if os.path.exists(path):
            return path
    raise SystemExit("未找到 Chrome/Edge，无法无头渲染 SVG")


def split_layers(svg_text: str):
    """把设计稿拆成 完整 / 背景 / 前景 三层内容。"""
    inner = re.search(r"<svg[^>]*>(.*)</svg>", svg_text, re.S).group(1)
    bg_gradient = re.search(r'<linearGradient id="background".*?</linearGradient>', svg_text, re.S).group(0)
    inner_bg = f"<defs>{bg_gradient}</defs><rect x=\"0\" y=\"0\" width=\"512\" height=\"512\" fill=\"url(#background)\"/>"
    inner_fg = re.sub(
        r'\s*<rect x="4" y="4" width="504" height="504" rx="71" fill="url\(#background\)"/>',
        "\n    ",
        inner,
        count=1,
    )
    # 主题图标层：只有卡片剪影 + 轨迹 + 起点圆点
    clip = re.search(r'<clipPath id="background-clip">.*?</clipPath>', svg_text, re.S).group(0)
    orbit = re.search(r'<path d="M311 57.*?"/>', svg_text, re.S).group(0)
    inner_mono = (
        f"<defs>{clip}</defs><g clip-path=\"url(#background-clip)\">"
        + orbit.replace('stroke="url(#orbit)"', 'stroke="#FFFFFF"')
        + '<circle cx="309" cy="57" r="13" fill="#FFFFFF"/>'
        + '<g transform="rotate(-5 256 256)">'
        + '<rect x="83" y="102" width="346" height="307" rx="37" fill="#FFFFFF"/></g></g>'
    )
    return inner, inner_bg, inner_fg, inner_mono


def render(chrome: str, inner: str, size: int, tag: str) -> Image.Image:
    html = (
        '<html><head><meta charset="utf-8"><style>'
        "html,body{margin:0;padding:0;background:transparent;overflow:hidden}"
        "</style></head><body>"
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" '
        f'viewBox="0 0 512 512">{inner}</svg></body></html>'
    )
    page = os.path.join(WORK, f"{tag}_{size}.html")
    png = os.path.join(WORK, f"{tag}_{size}.png")
    with open(page, "w", encoding="utf-8") as fh:
        fh.write(html)
    if os.path.exists(png):
        os.remove(png)
    subprocess.run(
        [
            chrome,
            "--headless=new",
            "--disable-gpu",
            "--hide-scrollbars",
            "--force-device-scale-factor=1",
            "--default-background-color=00000000",
            f"--window-size={size},{size}",
            f"--screenshot={png}",
            "file:///" + page.replace("\\", "/"),
        ],
        capture_output=True,
        timeout=180,
    )
    if not os.path.exists(png):
        raise SystemExit(f"渲染失败：{tag} {size}px")
    im = Image.open(png)
    if im.size != (size, size):
        raise SystemExit(f"渲染尺寸异常：{im.size} != ({size},{size})")
    return im


def main() -> None:
    chrome = find_chrome()
    os.makedirs(WORK, exist_ok=True)
    with open(SRC_SVG, encoding="utf-8") as fh:
        svg_text = fh.read()
    inner_full, inner_bg, inner_fg, inner_mono = split_layers(svg_text)

    # 旧式方形图标
    for d, size in LEGACY.items():
        render(chrome, inner_full, size, "full").convert("RGBA").save(
            os.path.join(RES, f"mipmap-{d}", "ic_launcher.png"), "PNG", optimize=True
        )
    # 自适应三层
    for d, size in ADAPTIVE.items():
        render(chrome, inner_bg, size, "bg").convert("RGB").save(
            os.path.join(RES, f"mipmap-{d}", "ic_launcher_background.webp"), "WEBP", quality=92, method=6
        )
        render(chrome, _scaled(inner_fg, FOREGROUND_SCALE), size, "fg").convert("RGBA").save(
            os.path.join(RES, f"mipmap-{d}", "ic_launcher_foreground.webp"), "WEBP", quality=92, method=6
        )
        render(chrome, _scaled(inner_mono, FOREGROUND_SCALE), size, "mono").convert("RGBA").save(
            os.path.join(RES, f"mipmap-{d}", "ic_launcher_monochrome.webp"), "WEBP", quality=92, method=6
        )
    # 应用内用图
    render(chrome, inner_fg, 256, "fg").convert("RGBA").save(
        os.path.join(COMPOSE, "ic_launcher_foreground.png"), "PNG", optimize=True
    )

    # 文档用 512 全图（README 等处展示）
    render(chrome, inner_full, 512, "doc").convert("RGBA").save(
        os.path.join(DOCS, "icon.png"), "PNG", optimize=True
    )

    # 预览：旧式 / 圆形遮罩 / 圆角方形遮罩 / 关于页组合
    size = 432
    bg = render(chrome, inner_bg, size, "bg").convert("RGB")
    fg = render(chrome, _scaled(inner_fg, FOREGROUND_SCALE), size, "fg").convert("RGBA")
    full = render(chrome, inner_full, size, "full").convert("RGBA")

    def masked(shape: str) -> Image.Image:
        # 真实启动器只显示图层中心的 72/108 区域，因此遮罩按该比例绘制，预览才与设备一致。
        canvas = bg.convert("RGBA")
        canvas.alpha_composite(fg)
        inset = int(size * (1 - 72 / 108) / 2)
        box = (inset, inset, size - 1 - inset, size - 1 - inset)
        mask = Image.new("L", (size, size), 0)
        d = ImageDraw.Draw(mask)
        if shape == "circle":
            d.ellipse(box, fill=255)
        else:
            d.rounded_rectangle(box, radius=int((size - 2 * inset) * 0.24), fill=255)
        out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        out.paste(canvas, (0, 0), mask)
        return out

    # 关于页组合：应用内底色为硬编码青绿 #27C7B7 + 前景图
    about = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    about_mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(about_mask).rounded_rectangle((0, 0, size - 1, size - 1), radius=int(size * 0.28), fill=255)
    about.paste(Image.new("RGBA", (size, size), (0x27, 0xC7, 0xB7, 255)), (0, 0), about_mask)
    pad = int(size * 0.12)
    about.alpha_composite(fg.resize((size - 2 * pad, size - 2 * pad), Image.LANCZOS), (pad, pad))

    sheet = Image.new("RGBA", (size * 4 + 150, size + 60), (24, 26, 30, 255))
    for i, img in enumerate([full, masked("circle"), masked("squircle"), about]):
        sheet.alpha_composite(img, (20 + i * (size + 33), 30))
    sheet.convert("RGB").save(os.path.join(DOCS, "icon-preview.png"), "PNG")

    mono = render(chrome, _scaled(inner_mono, FOREGROUND_SCALE), size, "mono").convert("RGBA")
    tinted = Image.new("RGBA", mono.size, (0, 0, 0, 0))
    tinted.paste(Image.new("RGBA", mono.size, (0x33, 0x5A, 0x8C, 255)), (0, 0), mono.split()[3])
    disk = Image.new("RGBA", mono.size, (0, 0, 0, 0))
    m = Image.new("L", mono.size, 0)
    ImageDraw.Draw(m).ellipse((0, 0, size - 1, size - 1), fill=255)
    disk.paste(tinted, (0, 0), m)
    disk.convert("RGB").save(os.path.join(DOCS, "icon-preview-monochrome.png"), "PNG")

    print("完成：图标资源与预览图已更新")


if __name__ == "__main__":
    main()
