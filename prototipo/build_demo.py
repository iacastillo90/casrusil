# Empaqueta el prototipo en un único HTML autocontenido para la demo (navegación en memoria).
import re, base64, os, sys
root = sys.argv[1]; out = sys.argv[2]
html = open(os.path.join(root, 'index.html'), encoding='utf-8').read()
def css(path):
    t = open(os.path.join(root, path), encoding='utf-8').read()
    def font(m):
        f = os.path.normpath(os.path.join(root, 'css', m.group(1)))
        return 'url("data:font/woff2;base64,' + base64.b64encode(open(f, 'rb').read()).decode() + '")'
    return re.sub(r'url\("(\.\./fonts/[^"]+)"\)', font, t)
html = re.sub(r'<link rel="stylesheet" href="([^"]+)">', lambda m: '<style>\n' + css(m.group(1)) + '\n</style>', html)
def js(m):
    t = open(os.path.join(root, m.group(1)), encoding='utf-8').read()
    assert '</script' not in t.lower(), m.group(1)
    return '<script>\n' + t + '\n</script>'
html = re.sub(r'<script src="([^"]+)"></script>', js, html)
html = html.replace('<html lang="es-CL" data-theme="light">', '<html lang="es-CL" data-theme="light" data-router="memory">')
html = html.replace('<title>SII-ERP AI · Prototipo del rediseño</title>', '<title>SII-ERP AI — Demo del rediseño v2</title>')
assert 'href="css/' not in html and 'src="js/' not in html
open(out, 'w', encoding='utf-8').write(html)
print(out, round(len(html) / 1024), 'KB')
