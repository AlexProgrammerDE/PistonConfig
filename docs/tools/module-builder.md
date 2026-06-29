---
layout: default
title: Module Builder
description: Select PistonConfig modules and generate Gradle or Maven dependency snippets.
---

# Module Builder

Select the modules your application needs. The generated snippets use the BOM so every module stays on the same version.

{: .lead }
`pistonconfig-core` is always selected because every other module depends on the core document model.

<div class="module-builder" data-module-builder></div>

## Common Presets

| Application shape | Modules |
| --- | --- |
| One YAML config file | core, yaml, migrations |
| CLI with deployment overrides | core, toml or yaml, env |
| Multi-format conversion tool | core plus every required format backend |
| Config classes with defaults | core, annotations, chosen backend |
| Shared typed keys | core, static-fields, chosen backend |

## Next Steps

After choosing modules:

1. Add the generated dependencies to your build.
2. Pick a [format backend](../guides/format-backends.html).
3. Follow the [startup order](../reference/startup-order.html).
4. Choose an [access style](../concepts/access-styles.html).
