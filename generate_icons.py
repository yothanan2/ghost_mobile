import os
from PIL import Image

# CONFIG
SOURCE_IMAGE = r"C:/Users/Your Mother/.gemini/antigravity/brain/c6129c42-0ecd-4b7a-86a9-ffabe5b1315e/ghost_icon_eye_command_1766684500779.png"
RES_DIR = r"android_app/app/src/main/res"

DENSITIES = {
    "mipmap-mdpi": (48, 48),
    "mipmap-hdpi": (72, 72),
    "mipmap-xhdpi": (96, 96),
    "mipmap-xxhdpi": (144, 144),
    "mipmap-xxxhdpi": (192, 192)
}

def generate_icons():
    if not os.path.exists(SOURCE_IMAGE):
        print(f"❌ Source image not found: {SOURCE_IMAGE}")
        return

    print(f"🎨 Load Source: {SOURCE_IMAGE}")
    img = Image.open(SOURCE_IMAGE)

    # Ensure transparency just in case (convert to RGBA)
    img = img.convert("RGBA")

    for folder, size in DENSITIES.items():
        out_dir = os.path.join(RES_DIR, folder)
        os.makedirs(out_dir, exist_ok=True)
        
        # Resize
        icon = img.resize(size, Image.Resampling.LANCZOS)
        
        # Save Square
        icon.save(os.path.join(out_dir, "ic_launcher.png"))
        
        # Save Round (For now using same, in prod we might mask it)
        # But honestly, Android 12+ handles adaptive icons better.
        # This is a good baseline.
        icon.save(os.path.join(out_dir, "ic_launcher_round.png"))
        
        print(f"✅ Generated {folder} ({size})")

if __name__ == "__main__":
    generate_icons()
