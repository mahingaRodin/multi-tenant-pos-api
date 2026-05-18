import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch
import matplotlib.patheffects as pe

# =========================
# THEME
# =========================
BG = "#0b1220"
SURFACE = "#111827"

COLORS = {
    "user": ("#38bdf8", "#0ea5e9", "👤"),
    "edge": ("#22d3ee", "#06b6d4", "🌐"),
    "backend": ("#6366f1", "#4f46e5", "⚙️"),
    "data": ("#f59e0b", "#d97706", "🗄️"),
    "async": ("#f97316", "#ea580c", "📩"),
    "payment": ("#10b981", "#059669", "💳"),
    "aws": ("#ff9900", "#f59e0b", "☁️"),
}

# =========================
# NODES (with icons)
# =========================
NODES = {
    "👤 Users": (0.5, 9.2, "user"),
    "🌐 API Gateway": (0.5, 8.0, "edge"),

    "⚙️ Auth Service": (0.5, 6.5, "backend"),
    "⚙️ Store Service": (2.2, 6.5, "backend"),
    "⚙️ Inventory Service": (3.9, 6.5, "backend"),
    "⚙️ Order Service": (5.6, 6.5, "backend"),
    "⚙️ Shift Reports": (7.3, 6.5, "backend"),
    "⚙️ Payment Service": (5.6, 4.8, "backend"),

    "🗄️ PostgreSQL (Multi-Tenant)": (2.5, 5.0, "data"),
    "🗄️ Redis Cache": (4.5, 5.0, "data"),
    "📩 RabbitMQ Queue": (7.3, 5.0, "async"),

    "💳 Stripe": (4.2, 3.2, "payment"),
    "💳 Razorpay": (5.6, 3.2, "payment"),
    "💳 Paypack": (7.0, 3.2, "payment"),

    "☁️ ALB": (1.2, 1.6, "aws"),
    "☁️ ECS/EKS": (2.6, 1.6, "aws"),
    "☁️ RDS": (4.0, 1.6, "aws"),
    "☁️ S3": (5.4, 1.6, "aws"),
    "☁️ SQS→Lambda": (6.8, 1.6, "aws"),
    "☁️ Cognito": (8.2, 1.6, "aws"),
    "☁️ CloudWatch": (9.0, 0.4, "aws"),
}

# =========================
# EDGES
# =========================
EDGES = [
    ("👤 Users", "🌐 API Gateway", "#38bdf8"),

    ("🌐 API Gateway", "⚙️ Auth Service", "#94a3b8"),
    ("⚙️ Auth Service", "⚙️ Store Service", "#94a3b8"),
    ("⚙️ Store Service", "⚙️ Inventory Service", "#94a3b8"),
    ("⚙️ Store Service", "⚙️ Order Service", "#94a3b8"),
    ("⚙️ Order Service", "⚙️ Payment Service", "#94a3b8"),
    ("⚙️ Order Service", "⚙️ Shift Reports", "#94a3b8"),

    ("⚙️ Auth Service", "🗄️ PostgreSQL (Multi-Tenant)", "#f59e0b"),
    ("⚙️ Store Service", "🗄️ PostgreSQL (Multi-Tenant)", "#f59e0b"),
    ("⚙️ Inventory Service", "🗄️ PostgreSQL (Multi-Tenant)", "#f59e0b"),
    ("⚙️ Order Service", "🗄️ PostgreSQL (Multi-Tenant)", "#f59e0b"),
    ("⚙️ Shift Reports", "🗄️ PostgreSQL (Multi-Tenant)", "#f59e0b"),

    ("⚙️ Store Service", "🗄️ Redis Cache", "#f59e0b"),
    ("⚙️ Inventory Service", "🗄️ Redis Cache", "#f59e0b"),

    ("⚙️ Order Service", "📩 RabbitMQ Queue", "#f97316"),
    ("📩 RabbitMQ Queue", "⚙️ Shift Reports", "#f97316"),

    ("⚙️ Payment Service", "💳 Stripe", "#10b981"),
    ("⚙️ Payment Service", "💳 Razorpay", "#10b981"),
    ("⚙️ Payment Service", "💳 Paypack", "#10b981"),

    ("🌐 API Gateway", "☁️ ALB", "#ff9900"),
    ("☁️ ALB", "☁️ ECS/EKS", "#ff9900"),
    ("☁️ ECS/EKS", "☁️ RDS", "#ff9900"),
    ("☁️ ECS/EKS", "☁️ S3", "#ff9900"),
    ("☁️ ECS/EKS", "☁️ SQS→Lambda", "#ff9900"),
    ("☁️ ECS/EKS", "☁️ Cognito", "#ff9900"),
    ("☁️ ECS/EKS", "☁️ CloudWatch", "#ff9900"),
]

# =========================
# FIGURE
# =========================
fig, ax = plt.subplots(figsize=(17, 11))
fig.patch.set_facecolor(BG)
ax.set_facecolor(BG)
ax.set_xlim(-0.5, 10.2)
ax.set_ylim(-0.3, 10.0)
ax.axis("off")

# =========================
# DRAW EDGES (thicker main flow)
# =========================
for src, dst, color in EDGES:
    x1, y1, _ = NODES[src]
    x2, y2, _ = NODES[dst]

    lw = 2.2 if "Users" in src or "API Gateway" in src else 1.1

    ax.annotate(
        "",
        xy=(x2, y2),
        xytext=(x1, y1),
        arrowprops=dict(
            arrowstyle="-|>",
            color=color,
            lw=lw,
            alpha=0.85,
            connectionstyle="arc3,rad=0.05",
        ),
    )

# =========================
# DRAW NODES (with glow effect)
# =========================
for label, (x, y, ctype) in NODES.items():
    border, fill, icon = COLORS[ctype]

    box = FancyBboxPatch(
        (x - 0.65, y - 0.40),
        1.3,
        0.8,
        boxstyle="round,pad=0.05",
        linewidth=1.5,
        edgecolor=border,
        facecolor=fill,
        alpha=0.92,
        zorder=3,
    )

    # Glow effect (important for LinkedIn aesthetic)
    box.set_path_effects([
        pe.withStroke(linewidth=6, foreground=border, alpha=0.15)
    ])

    ax.add_patch(box)

    ax.text(
        x,
        y,
        f"{icon}\n{label}",
        color="white",
        fontsize=7.8,
        fontweight="bold",
        ha="center",
        va="center",
        zorder=5,
    )

# =========================
# TITLE (POST STYLE)
# =========================
ax.text(
    5,
    9.85,
    "🚀 Multi-Tenant SaaS POS — Cloud-Native Architecture",
    color="white",
    fontsize=16,
    fontweight="bold",
    ha="center",
)

ax.text(
    5,
    9.45,
    "Spring Boot • PostgreSQL • Redis • RabbitMQ • AWS-ready Design",
    color="#94a3b8",
    fontsize=10,
    ha="center",
)

# =========================
# LEGEND
# =========================
legend_items = [
    mpatches.Patch(color=COLORS[k][0], label=k.upper())
    for k in COLORS.keys()
]

ax.legend(
    handles=legend_items,
    loc="upper right",
    framealpha=0.15,
    facecolor=SURFACE,
    edgecolor="#334155",
    labelcolor="white",
    fontsize=8,
)

# =========================
# EXPORT
# =========================
plt.tight_layout()
plt.savefig(
    "pos_architecture_linkedin.png",
    dpi=220,
    bbox_inches="tight",
    facecolor=BG,
)

print("Saved: pos_architecture_linkedin.png")