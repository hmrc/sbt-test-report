function removeSearchFromUrlParams() {
    const url = new URL(window.location);
    let clearUrlParams = url.href;
    if (url.search !== "") {
        clearUrlParams = url.href.replace(url.search, '');
    }
    history.pushState({}, "", clearUrlParams);
    return clearUrlParams;
}

function clearSearchFromUrl() {
    window.location.href = removeSearchFromUrlParams();
}

function debounce(func, delay) {
    let timeoutId;

    return function () {
        const context = this;
        const args = arguments;

        clearTimeout(timeoutId);

        timeoutId = setTimeout(function () {
            func.apply(context, args);
        }, delay);
    };
}

module.exports = {debounce, removeSearchFromUrlParams, clearSearchFromUrl};