(() => {
    const candidates = Array.from(document.querySelectorAll(
        'a, button, input, textarea, select, [role="button"], [contenteditable="true"]'
    ))

    const elements = []
    let index = 1

    for (let el of candidates) {
        const rect = el.getBoundingClientRect();

        // 跳过不可见的元素，避免模型点到隐藏按钮
        if (rect.width <= 0 || rect.height <= 0) {
            continue
        }

        const style = window.getComputedStyle(el)

        if (style.visibility === 'hidden' || style.display === 'none') {
            continue
        }

        const tag = el.tagName.toLowerCase()

        const text =
            el.innerText ||
            el.value ||
            el.getAttribute("placeholder") ||
            el.getAttribute("aria-label") ||
            el.getAttribute("title") ||
            '';

        el.setAttribute("data-agent-index",String(index))

        elements.push({
            index,
            selector: `[data-agent-index="${index}"]`,
            tag,
            text: String(text).trim().slice(0,120)
        })

        index += 1
    }

    return {
        url: location.href,
        title: document.title,
        scrollY: window.scrollY,
        viewportHeight: window.innerHeight,
        pageHeight: document.documentElement.scrollHeight,
        elements
    }
})()