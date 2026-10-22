import os, shutil, statistics, subprocess, time

N = 30
codes = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "codes"))
bench_dir = os.path.join(codes, "bench")
os.makedirs(bench_dir, exist_ok=True)

with open(os.path.join(bench_dir, "main.cpp"), "w") as f:
    f.write('#include <iostream>\nint main(){ int a,b; std::cin>>a>>b; std::cout<<a+b<<"\\n"; }\n')

subprocess.run(["docker", "exec", "judge_warm_cpp", "g++", "-std=c++17",
                "/code/bench/main.cpp", "-o", "/code/bench/main"], check=True)

warm_cmd = ["docker", "exec", "-i", "judge_warm_cpp", "/code/bench/main"]
cold_cmd = ["docker", "run", "--rm", "-i", "--network", "none", "--memory", "512m", "--cpus", "2",
            "-v", f"{codes}:/code", "judge-cpp", "/code/bench/main"]

def timed(cmd):
    t = time.perf_counter()
    subprocess.run(cmd, input="2 3\n", text=True, capture_output=True, check=True)
    return (time.perf_counter() - t) * 1000

def show(name, xs):
    xs = sorted(xs)
    p95 = xs[int(len(xs) * 0.95) - 1]
    print(f"{name}: mean {statistics.mean(xs):.0f} ms | median {statistics.median(xs):.0f} ms | p95 {p95:.0f} ms")

timed(warm_cmd)
timed(cold_cmd)

warm = [timed(warm_cmd) for _ in range(N)]
cold = [timed(cold_cmd) for _ in range(N)]

show("warm (docker exec)", warm)
show("cold (docker run)", cold)
print(f"speedup: {statistics.mean(cold) / statistics.mean(warm):.1f}x")

shutil.rmtree(bench_dir, ignore_errors=True)