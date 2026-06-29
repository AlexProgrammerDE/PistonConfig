(function () {
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
