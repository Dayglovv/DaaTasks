import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

AC_TOTAL = "Aho-Corasick (build + search)"
AC_SEARCH = "Aho-Corasick (search only)"
KMP_ALL = "KMP (once per pattern)"
KS = [10, 100, 1000]
NS = [1000, 10000, 100000]

data = {}
with open("results/tables/results.csv") as f:
    for r in csv.DictReader(f):
        data[(r["algorithm"], int(r["patterns"]), int(r["text_length"]))] = float(r["avg_ns"])


def decorate(ax, xlabel, title):
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel(xlabel)
    ax.set_title(title)
    ax.grid(True, which="both", linestyle=":", alpha=0.6)
fig, axes = plt.subplots(1, 3, figsize=(15, 4.8), sharey=True)
for ax, k in zip(axes, KS):
    ax.plot(NS, [data[(AC_TOTAL, k, n)] for n in NS], marker="o", label="Aho-Corasick (build + search)")
    ax.plot(NS, [data[(AC_SEARCH, k, n)] for n in NS], marker="x", linestyle=":", label="Aho-Corasick (search only)")
    ax.plot(NS, [data[(KMP_ALL, k, n)] for n in NS], marker="s", label="KMP (once per pattern)")
    decorate(ax, "Text length n, log scale", str(k) + " patterns")
    ax.legend(fontsize=8)
axes[0].set_ylabel("Average time (ns), log scale")
fig.suptitle("Execution time vs. text length")
fig.tight_layout()
fig.savefig("results/plots/time_vs_text_length.png", dpi=150)
plt.close(fig)
fig, axes = plt.subplots(1, 3, figsize=(15, 4.8), sharey=True)
for ax, n in zip(axes, NS):
    ax.plot(KS, [data[(AC_TOTAL, k, n)] for k in KS], marker="o", label="Aho-Corasick (build + search)")
    ax.plot(KS, [data[(KMP_ALL, k, n)] for k in KS], marker="s", label="KMP (once per pattern)")
    decorate(ax, "Number of patterns k, log scale", "text length " + str(n))
    ax.legend(fontsize=8)
axes[0].set_ylabel("Average time (ns), log scale")
fig.suptitle("Execution time vs. number of patterns")
fig.tight_layout()
fig.savefig("results/plots/time_vs_pattern_count.png", dpi=150)
plt.close(fig)

fig, ax = plt.subplots(figsize=(7, 5))
for n, marker in zip(NS, ("o", "s", "^")):
    ax.plot(KS, [data[(KMP_ALL, k, n)] / data[(AC_TOTAL, k, n)] for k in KS], marker=marker, label="n = " + str(n))
ax.axhline(1.0, color="gray", linestyle="--", linewidth=1, label="same speed")
decorate(ax, "Number of patterns k, log scale", "KMP time / Aho-Corasick time (above 1: Aho-Corasick is faster)")
ax.set_ylabel("Ratio, log scale")
ax.legend()
fig.tight_layout()
fig.savefig("results/plots/speedup.png", dpi=150)
plt.close(fig)
print("plots saved")
