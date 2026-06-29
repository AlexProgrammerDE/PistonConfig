(function () {
  const themeStorageKey = "pistonconfig-theme";
  const root = document.documentElement;
  const page = document.querySelector(".page");
  const themeToggle = document.querySelector("[data-theme-toggle]");
  const themeToggleLabel = document.querySelector("[data-theme-toggle-label]");
  const themeColor = document.querySelector("[data-theme-color]");
  const progress = document.querySelector("[data-reading-progress]");
  const breadcrumbs = document.querySelector("[data-breadcrumbs]");
  const searchOpen = document.querySelector("[data-search-open]");
  const searchDialog = document.querySelector("[data-search-dialog]");
  const searchClose = document.querySelector("[data-search-close]");
  const searchInput = document.querySelector("[data-search-input]");
  const searchResults = document.querySelector("[data-search-results]");
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
  const normalizePath = (path) => path.replace(/\/$/, "/index.html");
  const navLinks = Array.from(document.querySelectorAll(".sidebar a"));
  let activeNavLink = null;

  navLinks.forEach((link) => {
    const linkPath = normalizePath(new URL(link.href).pathname);
    if (linkPath === currentPath) {
      link.setAttribute("aria-current", "page");
      activeNavLink = link;
    }
  });

  const headings = Array.from(document.querySelectorAll(".page h2, .page h3"))
    .filter((heading) => !heading.closest(".quickstart, .home-panel, .link-card, .module-card, .path-card, .decision, .tool-card, .module-choice, .page-nav"));
  const toc = document.getElementById("page-toc");
  const tocLinks = [];

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
      tocLinks.push(link);
    });
  }

  if (tocLinks.length > 0 && "IntersectionObserver" in window) {
    const visibleHeadings = new Map();
    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          visibleHeadings.set(entry.target.id, entry.boundingClientRect.top);
        } else {
          visibleHeadings.delete(entry.target.id);
        }
      });

      const activeId = Array.from(visibleHeadings.entries())
        .sort((left, right) => left[1] - right[1])[0]?.[0];
      if (!activeId) {
        return;
      }

      tocLinks.forEach((link) => {
        link.setAttribute("aria-current", String(link.hash === `#${activeId}`));
      });
    }, { rootMargin: "-90px 0px -70% 0px" });

    headings.forEach((heading) => observer.observe(heading));
  }

  const updateProgress = () => {
    if (!progress) {
      return;
    }

    const scrollable = document.documentElement.scrollHeight - window.innerHeight;
    const value = scrollable <= 0 ? 0 : Math.min(1, Math.max(0, window.scrollY / scrollable));
    progress.style.width = `${value * 100}%`;
  };

  updateProgress();
  window.addEventListener("scroll", updateProgress, { passive: true });
  window.addEventListener("resize", updateProgress);

  if (breadcrumbs && page) {
    const currentTitle = page.dataset.pageTitle || document.title;
    const sectionTitle = activeNavLink?.closest(".nav-section")?.querySelector("p")?.textContent;

    if (currentPath !== "/index.html") {
      const home = document.createElement("a");
      home.href = "/";
      home.textContent = "Home";
      breadcrumbs.appendChild(home);

      if (sectionTitle) {
        const section = document.createElement("span");
        section.textContent = sectionTitle;
        breadcrumbs.appendChild(section);
      }

      const current = document.createElement("span");
      current.textContent = currentTitle;
      breadcrumbs.appendChild(current);
    }
  }

  if (page && activeNavLink) {
    const activeIndex = navLinks.indexOf(activeNavLink);
    const previousLink = navLinks[activeIndex - 1];
    const nextLink = navLinks[activeIndex + 1];

    if (previousLink || nextLink) {
      const pageNav = document.createElement("nav");
      pageNav.className = "page-nav";
      pageNav.setAttribute("aria-label", "Page navigation");

      if (previousLink) {
        pageNav.appendChild(pageNavLink(previousLink, "Previous"));
      } else {
        pageNav.appendChild(document.createElement("div"));
      }

      if (nextLink) {
        pageNav.appendChild(pageNavLink(nextLink, "Next"));
      }

      page.appendChild(pageNav);
    }
  }

  function pageNavLink(source, label) {
    const link = document.createElement("a");
    link.href = source.href;

    const caption = document.createElement("span");
    caption.textContent = label;
    link.appendChild(caption);

    const title = document.createElement("strong");
    title.textContent = source.textContent;
    link.appendChild(title);
    return link;
  }

  let searchIndex = null;

  const loadSearchIndex = async () => {
    if (searchIndex) {
      return searchIndex;
    }

    const response = await fetch(searchDialog.dataset.searchIndex, { headers: { Accept: "application/json" } });
    searchIndex = await response.json();
    return searchIndex;
  };

  const openSearch = async () => {
    if (!searchDialog) {
      return;
    }

    if (typeof searchDialog.showModal === "function") {
      searchDialog.showModal();
    } else {
      searchDialog.setAttribute("open", "");
    }

    await loadSearchIndex();
    renderSearchResults(searchInput.value);
    searchInput.focus();
  };

  const closeSearch = () => {
    if (!searchDialog) {
      return;
    }

    if (typeof searchDialog.close === "function") {
      searchDialog.close();
    } else {
      searchDialog.removeAttribute("open");
    }
  };

  const renderSearchResults = (query) => {
    if (!searchResults || !searchIndex) {
      return;
    }

    searchResults.replaceChildren();
    const normalizedQuery = query.trim().toLowerCase();
    const results = normalizedQuery
      ? searchIndex
        .map((item) => ({ item, score: searchScore(item, normalizedQuery) }))
        .filter((entry) => entry.score > 0)
        .sort((left, right) => right.score - left.score || left.item.title.localeCompare(right.item.title))
        .slice(0, 12)
        .map((entry) => entry.item)
      : searchIndex
        .filter((item) => ["/guides/getting-started.html", "/reference/modules.html", "/guides/format-backends.html", "/tools/module-builder.html"].includes(item.url))
        .slice(0, 6);

    if (results.length === 0) {
      const empty = document.createElement("p");
      empty.className = "search-empty";
      empty.textContent = "No matching pages.";
      searchResults.appendChild(empty);
      return;
    }

    results.forEach((item) => {
      const link = document.createElement("a");
      link.className = "search-result";
      link.href = item.url;
      link.setAttribute("role", "listitem");

      const title = document.createElement("strong");
      title.textContent = item.title;
      link.appendChild(title);

      const summary = document.createElement("p");
      summary.textContent = searchExcerpt(item, normalizedQuery);
      link.appendChild(summary);
      searchResults.appendChild(link);
    });
  };

  const searchScore = (item, query) => {
    const title = item.title.toLowerCase();
    const description = item.description.toLowerCase();
    const content = item.content.toLowerCase();
    let score = 0;

    if (title === query) {
      score += 100;
    }
    if (title.includes(query)) {
      score += 60;
    }
    if (description.includes(query)) {
      score += 30;
    }
    if (content.includes(query)) {
      score += 10;
    }

    for (const token of query.split(/\s+/).filter(Boolean)) {
      if (title.includes(token)) {
        score += 12;
      }
      if (description.includes(token)) {
        score += 6;
      }
      if (content.includes(token)) {
        score += 2;
      }
    }

    return score;
  };

  const searchExcerpt = (item, query) => {
    if (!query) {
      return item.description || item.content.slice(0, 180);
    }

    const content = item.content;
    const index = content.toLowerCase().indexOf(query);
    if (index < 0) {
      return item.description || content.slice(0, 180);
    }

    const start = Math.max(0, index - 70);
    const end = Math.min(content.length, index + query.length + 110);
    return `${start > 0 ? "... " : ""}${content.slice(start, end)}${end < content.length ? " ..." : ""}`;
  };

  if (searchOpen && searchDialog) {
    searchOpen.addEventListener("click", openSearch);
  }

  if (searchClose) {
    searchClose.addEventListener("click", closeSearch);
  }

  if (searchInput) {
    searchInput.addEventListener("input", () => renderSearchResults(searchInput.value));
  }

  document.addEventListener("keydown", (event) => {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
      event.preventDefault();
      openSearch();
    }

    if (event.key === "/" && !["INPUT", "TEXTAREA"].includes(document.activeElement?.tagName)) {
      event.preventDefault();
      openSearch();
    }
  });

  const moduleBuilder = document.querySelector("[data-module-builder]");
  if (moduleBuilder) {
    setupModuleBuilder(moduleBuilder);
  }

  function setupModuleBuilder(container) {
    const modules = [
      ["pistonconfig-core", "Core document model, loaders, codecs, comments, metadata, and merging.", true],
      ["pistonconfig-yaml", "YAML and YML files backed by SnakeYAML.", true],
      ["pistonconfig-properties", "Java properties files backed by Apache Commons Configuration.", false],
      ["pistonconfig-json", "JSON, JSONC, and JSON5 backed by json5-java.", false],
      ["pistonconfig-toml", "TOML backed by Night Config.", false],
      ["pistonconfig-hocon", "HOCON backed by Lightbend Config.", false],
      ["pistonconfig-annotations", "Annotation-based config classes.", false],
      ["pistonconfig-static-fields", "Static typed config key declarations.", false],
      ["pistonconfig-env", "Environment variable and system property overrides.", false],
      ["pistonconfig-migrations", "Versioned document migrations.", true]
    ];

    const choices = document.createElement("div");
    choices.className = "module-matrix doc-grid three";
    const output = document.createElement("div");
    output.className = "dependency-output";

    modules.forEach(([name, description, checked]) => {
      const card = document.createElement("section");
      card.className = "module-choice";

      const label = document.createElement("label");
      const input = document.createElement("input");
      input.type = "checkbox";
      input.value = name;
      input.checked = checked;
      input.disabled = name === "pistonconfig-core";
      input.addEventListener("change", () => renderModuleOutput(container, output));

      const title = document.createElement("span");
      title.textContent = name;
      label.append(input, title);

      const text = document.createElement("p");
      text.textContent = description;
      card.append(label, text);
      choices.appendChild(card);
    });

    const actions = document.createElement("div");
    actions.className = "module-builder-actions";

    const allButton = document.createElement("button");
    allButton.type = "button";
    allButton.textContent = "Select all";
    allButton.addEventListener("click", () => {
      choices.querySelectorAll("input").forEach((input) => {
        input.checked = true;
      });
      renderModuleOutput(container, output);
    });

    const minimalButton = document.createElement("button");
    minimalButton.type = "button";
    minimalButton.textContent = "Minimal YAML";
    minimalButton.addEventListener("click", () => {
      choices.querySelectorAll("input").forEach((input) => {
        input.checked = ["pistonconfig-core", "pistonconfig-yaml", "pistonconfig-migrations"].includes(input.value);
      });
      renderModuleOutput(container, output);
    });

    actions.append(allButton, minimalButton);
    container.append(choices, actions, output);
    renderModuleOutput(container, output);
  }

  function renderModuleOutput(container, output) {
    const selected = Array.from(container.querySelectorAll("input:checked")).map((input) => input.value);
    const dependencies = selected.map((name) => `  implementation("net.pistonmaster:${name}")`).join("\n");
    const mavenDependencies = selected.map((name) => [
      "  <dependency>",
      "    <groupId>net.pistonmaster</groupId>",
      `    <artifactId>${name}</artifactId>`,
      "  </dependency>"
    ].join("\n")).join("\n");

    output.replaceChildren(
      codeBlock("kotlin", [
        "dependencies {",
        '  implementation(platform("net.pistonmaster:pistonconfig-bom:0.1.0-SNAPSHOT"))',
        dependencies,
        "}"
      ].join("\n")),
      codeBlock("xml", [
        "<dependencies>",
        mavenDependencies,
        "</dependencies>"
      ].join("\n"))
    );
  }

  function codeBlock(language, text) {
    const frame = document.createElement("div");
    frame.className = "code-frame";

    const pre = document.createElement("pre");
    const code = document.createElement("code");
    code.className = `language-${language}`;
    code.textContent = text;
    pre.appendChild(code);
    frame.appendChild(pre);
    addCopyButton(frame);
    return frame;
  }

  document.querySelectorAll(".highlight").forEach((block) => {
    addCopyButton(block);
  });

  function addCopyButton(block) {
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
  }
})();
