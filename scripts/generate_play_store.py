#!/usr/bin/env python3
"""Export genuine Sudoku by Brendigo Play Store graphics from tracked assets."""
from pathlib import Path
from PIL import Image, ImageOps
import cairosvg, io, json, zipfile

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/"play-store-assets"
GRAPHICS=OUT/"graphics"
PHONE=OUT/"phone-1080x1920"
FILES=[
 ("03-game","01-gameplay.png"),
 ("01-home","02-home.png"),
 ("02-new-game","03-new-game.png"),
 ("05-daily","04-daily-challenge.png"),
 ("07-achievements","05-achievements.png"),
 ("06-statistics","06-statistics.png"),
 ("04-settings","07-settings.png"),
 ("08-splash","08-splash-optional.png"),
]
def render(svg, size):
    return Image.open(io.BytesIO(cairosvg.svg2png(bytestring=svg.encode("utf-8"),
                          output_width=size[0],output_height=size[1]))).convert("RGBA")
def main():
    GRAPHICS.mkdir(parents=True,exist_ok=True)
    PHONE.mkdir(parents=True,exist_ok=True)
    icon=(ROOT/"branding/sudoku-icon.svg").read_text(encoding="utf-8")
    banner=(ROOT/"branding/sudoku-feature-graphic.svg").read_text(encoding="utf-8")
    logo=(ROOT/"branding/sudoku-logo.svg").read_text(encoding="utf-8")
    # Full-square Google Play icon. Original artwork preserved, dark backing added.
    bg=Image.new("RGBA",(512,512),"#06152e")
    bg.alpha_composite(render(icon,(512,512)))
    iconfile=GRAPHICS/"01-google-play-icon-512x512.png"
    bg.save(iconfile,format="PNG",optimize=True)
    assert iconfile.stat().st_size<=1024*1024
    render(banner,(1024,500)).convert("RGB").save(
        GRAPHICS/"02-feature-graphic-HR-1024x500.png",format="PNG",optimize=True)
    banner_en=banner.replace("TRENIRAJ MOZAK SVAKI DAN","TRAIN YOUR MIND EVERY DAY")
    render(banner_en,(1024,500)).convert("RGB").save(
        GRAPHICS/"03-feature-graphic-EN-1024x500.png",format="PNG",optimize=True)
    render(logo,(1400,420)).save(
        GRAPHICS/"04-brand-logo-transparent-1400x420.png",format="PNG",optimize=True)
    evidence=[]
    for src,filename in FILES:
        path=ROOT/"docs/screenshots"/(src+".webp")
        if not path.is_file():
            raise FileNotFoundError("Actual emulator screenshot missing: "+str(path))
        with Image.open(path) as original:
            original.load()
            original_size=original.size
            assert original.width>=320 and original.height>=320
            converted=ImageOps.fit(original.convert("RGB"),(1080,1920),
                                  method=Image.Resampling.LANCZOS)
            converted.save(PHONE/filename,format="PNG",optimize=True)
        evidence.append({"filename":filename,"original":str(path.relative_to(ROOT)),
                         "original_dimensions":original_size,
                         "export_dimensions":[1080,1920],
                         "capture":"real Android emulator screenshot",
                         "optional":filename.endswith("optional.png")})
    manifest={"app":"Sudoku by Brendigo","author":"Brendigo",
              "website":"https://brendigo.com",
              "source":"https://github.com/bren-wp/Sudoku",
              "screenshots":evidence}
    (OUT/"manifest.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    (OUT/"README-HR.md").write_text("""# Sudoku by Brendigo – Google Play paket

Autor: Brendigo · https://brendigo.com

Grafike za izravni prijenos u Google Play Console:
- graphics/01-google-play-icon-512x512.png: ikona 512 × 512, PNG RGBA, <= 1 MB.
- graphics/02-feature-graphic-HR-1024x500.png: naslovna slika HR 1024 × 500, PNG RGB.
- graphics/03-feature-graphic-EN-1024x500.png: naslovna slika EN 1024 × 500, PNG RGB.
- phone-1080x1920/01-gameplay.png do 07-settings.png: 7 stvarnih snimki aplikacije, 1080 × 1920, PNG RGB.
- phone-1080x1920/08-splash-optional.png: dodatni stvarni splash (nije prioritet za objavu).
- graphics/04-brand-logo-transparent-1400x420.png: bonus logotip, nije obavezni Play element.

Izvor: docs/screenshots/*.webp u ovom repozitoriju, iz stvarnog Android emulatora.
Format je prilagođen na 9:16 uz vrlo malo obrezivanje ruba jer su
izvornici 941 × 1672. Nisu generirani izmišljeni zasloni.
Prije objave obavezno usporedi snimke s verzijom AAB-a koja se objavljuje.

7-inčni i 10-inčni tableti: priložiti snimke stvarnog prikaza s tableta
(idealno 1080×1920 uspravno ili 1920×1080 vodoravno), a NE rastegnute
prikaze mobitela. Isto pravilo vrijedi za Chromebook. Ako aplikacija
ne podržava TV, Wear OS, Auto ili XR, grafike za njih nisu potrebne.

https://support.google.com/googleplay/android-developer/answer/9866151
""",encoding="utf-8")
    for file in PHONE.glob("*.png"):
        with Image.open(file) as check:
            assert check.size==(1080,1920) and check.mode=="RGB",file
    for file in GRAPHICS.glob("*.png"):
        with Image.open(file) as check:
            assert check.size in ((512,512),(1024,500),(1400,420)),file
    target=OUT/"Sudoku-by-Brendigo-Google-Play-Assets.zip"
    with zipfile.ZipFile(target,"w",zipfile.ZIP_DEFLATED,compresslevel=6) as z:
        for f in sorted(OUT.rglob("*")):
            if f.is_file() and f!=target:
                z.write(f,f.relative_to(OUT))
    print("Exported",len(FILES),"authentic screenshots and 4 branded assets")
    print("ZIP",target,target.stat().st_size,"bytes")
if __name__=="__main__":
    main()
