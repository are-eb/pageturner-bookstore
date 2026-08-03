/**
 * Pageturner — supreme interaction layer.
 * Loaded on every page via the head-assets fragment. Progressive enhancement only:
 * every feature here degrades silently if unsupported, and everything respects
 * prefers-reduced-motion plus touch/coarse-pointer devices.
 */
(function () {
  "use strict";

  const prefersReduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const isFinePointer = window.matchMedia("(pointer: fine)").matches;

  document.addEventListener("DOMContentLoaded", () => {
    initScrollReveal();
    initHeaderScrollState();
    initToasts();
    initQuantitySteppers();
    initPasswordToggles();
    if (isFinePointer && !prefersReduced) {
      initBookTilt();
      initCursorGlow();
    }
  });

  /* ---------- Toasts: auto-dismiss after 5s, or on manual close ---------- */
  function initToasts() {
    document.querySelectorAll(".toast").forEach((toast) => {
      const dismiss = () => {
        if (toast.classList.contains("is-leaving")) return;
        toast.classList.add("is-leaving");
        setTimeout(() => toast.remove(), prefersReduced ? 0 : 220);
      };
      const timer = setTimeout(dismiss, 5000);
      const closeBtn = toast.querySelector(".toast-close");
      if (closeBtn) {
        closeBtn.addEventListener("click", () => {
          clearTimeout(timer);
          dismiss();
        });
      }
    });
  }

  /* ---------- Quantity steppers: +/- buttons next to number inputs ---------- */
  function initQuantitySteppers() {
    document.querySelectorAll("[data-qty-stepper]").forEach((wrap) => {
      const input = wrap.querySelector("input[type=number]");
      if (!input) return;
      wrap.querySelectorAll("[data-qty-step]").forEach((btn) => {
        btn.addEventListener("click", () => {
          const step = parseInt(btn.dataset.qtyStep, 10);
          const min = input.min ? parseInt(input.min, 10) : 1;
          const max = input.max ? parseInt(input.max, 10) : Infinity;
          const current = parseInt(input.value, 10) || min;
          const next = Math.min(max, Math.max(min, current + step));
          input.value = next;
          input.dispatchEvent(new Event("change", { bubbles: true }));
        });
      });
    });
  }

  /* ---------- Password visibility toggle ---------- */
  function initPasswordToggles() {
    document.querySelectorAll("[data-password-toggle]").forEach((btn) => {
      const input = document.getElementById(btn.dataset.passwordToggle);
      if (!input) return;
      btn.addEventListener("click", () => {
        const showing = input.type === "text";
        input.type = showing ? "password" : "text";
        btn.textContent = showing ? "Show" : "Hide";
      });
    });
  }

  /* ---------- Scroll reveal ---------- */
  function initScrollReveal() {
    const targets = document.querySelectorAll(".reveal");
    if (!targets.length) return;

    if (prefersReduced || !("IntersectionObserver" in window)) {
      targets.forEach((t) => t.classList.add("is-visible"));
      return;
    }

    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry, i) => {
          if (entry.isIntersecting) {
            const delay = Math.min(parseInt(entry.target.dataset.revealDelay || 0, 10), 400);
            setTimeout(() => entry.target.classList.add("is-visible"), delay);
            io.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12, rootMargin: "0px 0px -40px 0px" }
    );
    targets.forEach((t) => io.observe(t));
  }

  /* ---------- Header darkens/shadows once the page scrolls ---------- */
  function initHeaderScrollState() {
    const header = document.querySelector(".site-header");
    if (!header) return;
    const toggle = () => header.classList.toggle("is-scrolled", window.scrollY > 8);
    toggle();
    window.addEventListener("scroll", toggle, { passive: true });
  }

  /* ---------- 3D tilt on book covers, following the pointer ---------- */
  function initBookTilt() {
    const wraps = document.querySelectorAll(".book-cover-wrap");
    wraps.forEach((wrap) => {
      wrap.addEventListener("mousemove", (e) => {
        const rect = wrap.getBoundingClientRect();
        const px = (e.clientX - rect.left) / rect.width - 0.5;
        const py = (e.clientY - rect.top) / rect.height - 0.5;
        const rotateY = px * 14;
        const rotateX = -py * 14;
        wrap.style.transform = `rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
      });
      wrap.addEventListener("mouseleave", () => {
        wrap.style.transform = "rotateX(0deg) rotateY(0deg)";
      });
    });
  }

  /* ---------- Faint brass glow that follows the cursor across dark surfaces ---------- */
  function initCursorGlow() {
    const zones = document.querySelectorAll("[data-cursor-glow]");
    if (!zones.length) return;

    const glow = document.createElement("div");
    glow.id = "cursor-glow";
    document.body.insertBefore(glow, document.body.firstChild);

    let active = null;

    zones.forEach((zone) => {
      zone.addEventListener("mouseenter", () => {
        active = zone;
        glow.style.opacity = "1";
      });
      zone.addEventListener("mouseleave", () => {
        if (active === zone) {
          active = null;
          glow.style.opacity = "0";
        }
      });
    });

    document.addEventListener("mousemove", (e) => {
      if (!active) return;
      glow.style.transform = `translate(${e.clientX}px, ${e.clientY}px)`;
    });
  }
})();
