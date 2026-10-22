import json, statistics, time, urllib.request
from concurrent.futures import ThreadPoolExecutor

URL = "http://localhost:8080/problems/submit"
TOKEN = "PASTE_JWT_HERE"
EMAIL = "your_email"
SLUG = "slug"
LANGUAGE = "cpp"
TOTAL = 20
LEVELS = [1, 5, 10, 10, 10]

import os
code = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "solution.txt"), encoding="utf-8").read()

def submit(_):
    body = json.dumps({"code": code, "language": LANGUAGE, "slug": SLUG, "email": EMAIL}).encode()
    req = urllib.request.Request(URL, data=body, method="POST", headers={
        "Content-Type": "application/json",
        "Authorization": "Bearer " + TOKEN,
    })
    t = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=300) as r:
            verdict = json.loads(r.read()).get("verdict", "?")
    except Exception as e:
        verdict = "ERROR " + str(e)
    return (time.perf_counter() - t) * 1000, verdict

print("warm-up:", submit(0))

for level in LEVELS:
    start = time.perf_counter()
    with ThreadPoolExecutor(max_workers=level) as pool:
        results = list(pool.map(submit, range(TOTAL)))
    wall = time.perf_counter() - start

    lat = sorted(r[0] for r in results)
    accepted = sum(1 for r in results if "Accepted" in r[1])
    others = {r[1] for r in results if "Accepted" not in r[1]}
    p95 = lat[int(len(lat) * 0.95) - 1]
    print(f"concurrency {level:>2}: accepted {accepted}/{TOTAL} | median {statistics.median(lat):.0f} ms | "
          f"p95 {p95:.0f} ms | throughput {TOTAL / wall:.1f} submissions/s")
    if others:
        print("   other verdicts:", others)