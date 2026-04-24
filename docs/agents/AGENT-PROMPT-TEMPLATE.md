Please read and analyze the following foundational documents from the current workspace:
1. `AGENT_ANCHOR.md` (for core system constraints and architectural rules)
2. `CONTEXT-BLOCK.md` (for the overarching system context and domain rules)
3. `AGENT-PROMPT-GUIDE.md` (for the exact structure, tone, and formatting template)
4. `WORK_PACKAGES.md` (for the specific features, technical requirements, and acceptance criteria)

Your task is to generate a comprehensive, component-wise Agent Prompt Guide specifically for the Work Package: **XX**.

Instructions for generation:
- Extract all relevant technical requirements, features, UI/UX guidelines, and database constraints specifically for **XX** from `WORK_PACKAGES.md`.
- Apply the core philosophy, tone, and strict constraints defined in `AGENT_ANCHOR.md` and `CONTEXT-BLOCK.md`.
- Format the entire output strictly matching the template and structure outlined in `AGENT-PROMPT-GUIDE.md`.
- Do not invent new features; stick strictly to what is defined for XX.
- Provide the final result entirely within a single markdown code block and add a comment at the top indicating this should be saved as `AGENT-PROMPT-GUIDE-XX.md`.
