(function () {
  const themeStorageKey = "pistonconfig-theme";
  const root = document.documentElement;
  const themeToggle = document.querySelector("[data-theme-toggle]");
  const themeToggleLabel = document.querySelector("[data-theme-toggle-label]");
  const themeColor = document.querySelector("[data-theme-color]");
  const darkQuery = window.matchMedia ? window.matchMedia("(prefers-color-scheme: dark)") : null;

  const storedTheme = () => {
    try {
      const value = window.localStorage.getItem(themeStorageKey);
      return value === "dark" || value === "light" ? value : null;
    } catch (error) {
      return null;
    }
  };

  const systemTheme = () => darkQuery && darkQuery.matches ? "dark" : "light";

  const applyTheme = (theme, persist) => {
    root.dataset.theme = theme;
    root.style.colorScheme = theme;

    if (themeColor) {
      themeColor.setAttribute("content", theme === "dark" ? "#11120f" : "#fbfbf8");
    }

    if (themeToggle && themeToggleLabel) {
      const nextTheme = theme === "dark" ? "light" : "dark";
      themeToggleLabel.textContent = nextTheme === "dark" ? "Dark" : "Light";
      themeToggle.setAttribute("aria-label", `Switch to ${nextTheme} mode`);
      themeToggle.setAttribute("aria-pressed", String(theme === "dark"));
    }

    if (persist) {
      try {
        window.localStorage.setItem(themeStorageKey, theme);
      } catch (error) {
        // Theme persistence is optional.
      }
    }
  };

  applyTheme(root.dataset.theme === "dark" || root.dataset.theme === "light" ? root.dataset.theme : storedTheme() || systemTheme(), false);

  if (themeToggle) {
    themeToggle.addEventListener("click", () => {
      applyTheme(root.dataset.theme === "dark" ? "light" : "dark", true);
    });
  }

  const handleSystemThemeChange = () => {
    if (!storedTheme()) {
      applyTheme(systemTheme(), false);
    }
  };

  if (darkQuery?.addEventListener) {
    darkQuery.addEventListener("change", handleSystemThemeChange);
  } else if (darkQuery?.addListener) {
    darkQuery.addListener(handleSystemThemeChange);
  }

  const currentPath = window.location.pathname.replace(/\/$/, "/index.html");

  document.querySelectorAll(".sidebar a").forEach((link) => {
    const linkPath = new URL(link.href).pathname.replace(/\/$/, "/index.html");
    if (linkPath === currentPath) {
      link.setAttribute("aria-current", "page");
    }
  });

  const headings = Array.from(document.querySelectorAll(".page h2, .page h3"))
    .filter((heading) => !heading.closest(".home-panel, .link-card, .module-card, .path-card, .decision"));
  const toc = document.getElementById("page-toc");

  if (toc && headings.length > 0) {
    headings.forEach((heading) => {
      if (!heading.id) {
        heading.id = heading.textContent
          .trim()
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, "-")
          .replace(/^-|-$/g, "");
      }

      const link = document.createElement("a");
      link.href = `#${heading.id}`;
      link.textContent = heading.textContent;
      link.dataset.level = heading.tagName === "H3" ? "3" : "2";
      toc.appendChild(link);
    });
  }

  document.querySelectorAll(".highlight").forEach((block) => {
    const code = block.querySelector("pre");
    if (!code) {
      return;
    }

    const button = document.createElement("button");
    button.type = "button";
    button.className = "copy-code";
    button.textContent = "Copy";
    button.addEventListener("click", async () => {
      if (!navigator.clipboard) {
        return;
      }
      await navigator.clipboard.writeText(code.innerText);
      button.textContent = "Copied";
      window.setTimeout(() => {
        button.textContent = "Copy";
      }, 1200);
    });
    block.appendChild(button);
  });
})();
