import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Draws the maze to a PNG file. Ready to use, you do not need to change it. */
public class Render {

    /** Walls that are not broken are black lines, the way out is red, the start is green, the exit is blue. */
    public static void draw(Input.Walls w, int[] broken, int[] path, String file) {
        try {
            int rows = w.rows(), cols = w.cols();
            int s = Math.max(2, Math.min(24, 2000 / Math.max(rows, cols)));
            int m = 10;
            BufferedImage img = new BufferedImage(cols * s + 2 * m, rows * s + 2 * m, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());
            boolean[] open = new boolean[w.count()];
            for (int k : broken) open[k] = true;
            g.setColor(Color.BLACK);
            g.drawRect(m, m, cols * s, rows * s);
            for (int k = 0; k < w.count(); k++) {
                if (open[k]) continue;
                int a = Math.min(w.a()[k], w.b()[k]), b = Math.max(w.a()[k], w.b()[k]);
                int r = a / cols, c = a % cols;
                if (b == a + 1) g.drawLine(m + (c + 1) * s, m + r * s, m + (c + 1) * s, m + (r + 1) * s);
                else g.drawLine(m + c * s, m + (r + 1) * s, m + (c + 1) * s, m + (r + 1) * s);
            }
            g.setColor(Color.RED);
            for (int i = 0; i + 1 < path.length; i++) {
                g.drawLine(m + (path[i] % cols) * s + s / 2, m + (path[i] / cols) * s + s / 2,
                           m + (path[i + 1] % cols) * s + s / 2, m + (path[i + 1] / cols) * s + s / 2);
            }
            int d = Math.max(3, s / 2);
            g.setColor(new Color(0, 150, 0));
            g.fillOval(m + s / 2 - d / 2, m + s / 2 - d / 2, d, d);
            g.setColor(Color.BLUE);
            g.fillOval(m + (cols - 1) * s + s / 2 - d / 2, m + (rows - 1) * s + s / 2 - d / 2, d, d);
            g.dispose();
            new File(file).getAbsoluteFile().getParentFile().mkdirs();
            ImageIO.write(img, "png", new File(file));
        } catch (IOException e) {
            System.out.println("Could not write " + file + ": " + e.getMessage());
        }
    }

    /**
     * Writes a page you can open with a double click: your maze, with the numbers of your files on it.
     * You can walk through the maze with the arrow keys. Ready to use.
     */
    public static void page(Input.Walls w, Input.Slides s, int[] broken, int[] path, int[] zoneOf, String file) {
        boolean[] open = new boolean[w.count()];
        for (int k : broken) open[k] = true;
        StringBuilder d = new StringBuilder();
        d.append("{\"rows\":").append(w.rows()).append(",\"cols\":").append(w.cols());
        d.append(",\"a\":").append(java.util.Arrays.toString(w.a()));
        d.append(",\"b\":").append(java.util.Arrays.toString(w.b()));
        d.append(",\"cost\":").append(java.util.Arrays.toString(w.cost()));
        d.append(",\"open\":[");
        for (int k = 0; k < open.length; k++) d.append(k > 0 ? "," : "").append(open[k] ? 1 : 0);
        d.append("],\"order\":").append(java.util.Arrays.toString(broken));
        d.append(",\"path\":").append(java.util.Arrays.toString(path));
        d.append(",\"zone\":").append(java.util.Arrays.toString(zoneOf));
        d.append(",\"from\":").append(java.util.Arrays.toString(s.from()));
        d.append(",\"to\":").append(java.util.Arrays.toString(s.to()));
        d.append(",\"time\":").append(java.util.Arrays.toString(s.time()));
        d.append("}");
        try {
            new File(file).getAbsoluteFile().getParentFile().mkdirs();
            try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
                out.print(PAGE.replace("__DATA__", d.toString()));
            }
        } catch (IOException e) {
            System.out.println("Could not write " + file + ": " + e.getMessage());
        }
    }

    private static final String PAGE = """
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><title>My maze</title>
<style>
body{font-family:system-ui,Segoe UI,Arial,sans-serif;margin:0;background:#f6f5f1;color:#222}
header{padding:14px 20px 6px}h1{font-size:20px;margin:0 0 4px}
.sub{font-size:13px;color:#555}
.bar{padding:6px 20px;display:flex;flex-wrap:wrap;gap:6px 18px;font-size:14px}
.bar label{cursor:pointer;white-space:nowrap}
button{font:inherit;padding:3px 10px;border:1px solid #999;background:#fff;border-radius:4px;cursor:pointer}
#wrap{padding:6px 20px}canvas{background:#fff;border:1px solid #ccc;max-width:100%}
#msg{padding:4px 20px 2px;font-size:15px;min-height:22px}
#info{padding:0 20px 16px;font-size:13px;color:#444;min-height:18px}
</style></head><body>
<header><h1>My maze</h1>
<div class="sub">Green dot: start (cell 0). Blue dot: exit (last cell). Walk with the arrow keys or W A S D. A wall that is not drawn is broken: there is a door. Move the mouse over a wall to read its line in walls.txt.</div></header>
<div class="bar">
<label><input type="checkbox" id="cNum"> cell numbers</label>
<label><input type="checkbox" id="cCost"> wall costs</label>
<label><input type="checkbox" id="cPath"> way out (Mission 3)</label>
<label><input type="checkbox" id="cZone"> zones (Mission 4)</label>
<label><input type="checkbox" id="cSlide"> slides</label>
<button id="build">Watch the maze being built (Mission 1)</button> <button id="reset">Start again</button></div>
<div id="msg"></div>
<div id="wrap"><canvas id="cv"></canvas></div>
<div id="info"></div>
<script>
const D = __DATA__;
const R = D.rows, C = D.cols, N = R * C;
const M = 14;
let S = Math.max(14, Math.min(48, Math.floor((Math.min(window.innerWidth, 1100) - 60) / C)));
const cv = document.getElementById('cv'), g = cv.getContext('2d');
cv.width = C * S + 2 * M; cv.height = R * S + 2 * M;
const key = (a, b) => Math.min(a, b) * N + Math.max(a, b);
const door = new Set();
const wallOf = new Map();
const openNow = D.open.slice();
D.a.forEach((a, k) => { if (openNow[k]) door.add(key(a, D.b[k])); });
const cx = c => M + (c % C) * S + S / 2, cy = c => M + Math.floor(c / C) * S + S / 2;
function seg(k) {
  const a = Math.min(D.a[k], D.b[k]), b = Math.max(D.a[k], D.b[k]);
  const r = Math.floor(a / C), c = a % C;
  if (b === a + 1) return [M + (c + 1) * S, M + r * S, M + (c + 1) * S, M + (r + 1) * S];
  return [M + c * S, M + (r + 1) * S, M + (c + 1) * S, M + (r + 1) * S];
}
let me = 0, moves = 0, seen = new Set([0]), done = false;
const msg = document.getElementById('msg'), info = document.getElementById('info');
const on = id => document.getElementById(id).checked;
function pathLen() { return D.path.length ? D.path.length - 1 : -1; }
function draw() {
  g.clearRect(0, 0, cv.width, cv.height);
  if (on('cZone')) {
    const zs = Math.max(...D.zone) + 1;
    if (zs > 1) for (let c = 0; c < N; c++) {
      g.fillStyle = 'hsl(' + Math.round(D.zone[c] * 360 / zs * 7 % 360) + ',70%,85%)';
      g.fillRect(M + (c % C) * S, M + Math.floor(c / C) * S, S, S);
    }
  }
  g.fillStyle = 'rgba(255,200,0,0.25)';
  seen.forEach(c => g.fillRect(M + (c % C) * S + 2, M + Math.floor(c / C) * S + 2, S - 4, S - 4));
  if (on('cSlide')) {
    g.strokeStyle = 'rgba(120,60,180,0.35)'; g.lineWidth = 1;
    D.from.forEach((f, i) => { g.beginPath(); g.moveTo(cx(f), cy(f)); g.lineTo(cx(D.to[i]), cy(D.to[i])); g.stroke(); });
  }
  g.strokeStyle = '#111'; g.lineWidth = 2;
  g.strokeRect(M, M, C * S, R * S);
  for (let k = 0; k < D.a.length; k++) {
    if (openNow[k]) continue;
    const q = seg(k);
    g.strokeStyle = '#111'; g.lineWidth = 2;
    g.beginPath(); g.moveTo(q[0], q[1]); g.lineTo(q[2], q[3]); g.stroke();
  }
  if (on('cPath') && D.path.length > 1) {
    g.strokeStyle = '#e02020'; g.lineWidth = 3; g.beginPath();
    D.path.forEach((c, i) => { if (i) g.lineTo(cx(c), cy(c)); else g.moveTo(cx(c), cy(c)); });
    g.stroke();
  }
  g.font = Math.max(8, Math.floor(S / 4)) + 'px sans-serif'; g.textAlign = 'center'; g.textBaseline = 'middle';
  if (on('cNum')) { g.fillStyle = '#555'; for (let c = 0; c < N; c++) g.fillText(c, M + (c % C) * S + S * 0.22, M + Math.floor(c / C) * S + S * 0.2); }
  if (on('cCost')) {
    g.fillStyle = '#0a5';
    for (let k = 0; k < D.a.length; k++) { if (openNow[k]) continue; const q = seg(k); g.fillText(D.cost[k], (q[0] + q[2]) / 2, (q[1] + q[3]) / 2); }
  }
  const r = Math.max(4, S / 4);
  const dot = (c, col) => { g.fillStyle = col; g.beginPath(); g.arc(cx(c), cy(c), r, 0, 7); g.fill(); };
  dot(0, '#1a9a1a'); dot(N - 1, '#1a4ee0');
  g.fillStyle = '#ffb000'; g.strokeStyle = '#333'; g.lineWidth = 1.5;
  g.beginPath(); g.arc(cx(me), cy(me), r * 1.15, 0, 7); g.fill(); g.stroke();
}
function step(dr, dc) {
  if (done) return;
  const r = Math.floor(me / C), c = me % C, nr = r + dr, nc = c + dc;
  if (nr < 0 || nc < 0 || nr >= R || nc >= C) return;
  const t = nr * C + nc;
  if (!door.has(key(me, t))) { msg.textContent = door.size === 0 ? 'There are no doors yet. Finish Mission 1: your program must break the walls.' : 'A wall.'; return; }
  me = t; moves++; seen.add(t); msg.textContent = 'Moves: ' + moves;
  if (me === N - 1) {
    done = true; const p = pathLen();
    msg.textContent = 'You are out in ' + moves + ' moves.' + (p >= 0 ? ' The shortest way is ' + p + ' (Path length of your program).' : '');
  }
  draw();
}
document.addEventListener('keydown', e => {
  const m = { ArrowUp: [-1, 0], w: [-1, 0], ArrowDown: [1, 0], s: [1, 0], ArrowLeft: [0, -1], a: [0, -1], ArrowRight: [0, 1], d: [0, 1] }[e.key];
  if (m) { e.preventDefault(); step(m[0], m[1]); }
});
let timer = null;
document.getElementById('build').onclick = () => {
  if (timer) clearInterval(timer);
  if (!D.order.length) { msg.textContent = 'Nothing to show: your program has not broken any wall yet.'; return; }
  const final = D.open.slice();
  openNow.fill(0); door.clear(); me = 0; moves = 0; seen = new Set([0]); done = false; let i = 0;
  timer = setInterval(() => {
    const k = D.order[i++];
    openNow[k] = 1; door.add(key(D.a[k], D.b[k]));
    msg.textContent = 'Wall ' + i + ' of ' + D.order.length + ' broken: line ' + (k + 1) + ' of walls.txt, cells ' + D.a[k] + ' and ' + D.b[k] + '.';
    draw();
    if (i >= D.order.length) { clearInterval(timer); timer = null; msg.textContent = 'Done: ' + D.order.length + ' walls broken, ' + (D.a.length - D.order.length) + ' stand.'; }
  }, Math.max(15, Math.min(120, 4000 / D.order.length)));
};
document.getElementById('reset').onclick = () => { me = 0; moves = 0; seen = new Set([0]); done = false; msg.textContent = ''; draw(); };
document.querySelectorAll('.bar input').forEach(i => i.onchange = draw);
cv.addEventListener('mousemove', e => {
  const b = cv.getBoundingClientRect(), sx = cv.width / b.width;
  const x = (e.clientX - b.left) * sx, y = (e.clientY - b.top) * sx;
  let best = -1, bd = S / 3;
  for (let k = 0; k < D.a.length; k++) {
    const q = seg(k), d = Math.hypot((q[0] + q[2]) / 2 - x, (q[1] + q[3]) / 2 - y);
    if (d < bd) { bd = d; best = k; }
  }
  info.textContent = best < 0 ? '' : 'Line ' + (best + 1) + ' of walls.txt: "' + D.a[best] + ' ' + D.b[best] + ' ' + D.cost[best] + '". The wall between cell ' + D.a[best] + ' and cell ' + D.b[best] + ', cost ' + D.cost[best] + ' to break. ' + (openNow[best] ? 'It is broken now.' : 'It stands.');
});
if (door.size === 0) msg.textContent = 'Your maze is closed: every wall stands. Mission 1 will open the doors.';
draw();
</script></body></html>
""";
}
