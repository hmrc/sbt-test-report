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

module.exports = {removeSearchFromUrlParams, clearSearchFromUrl};