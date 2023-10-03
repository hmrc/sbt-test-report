export function updateUrlParam(url, key, value) {
    if (url) {
        if (value) url.searchParams.set(key, value);
        else if(value === undefined || value === '') url.searchParams.delete(key);
        history.pushState({}, "", url.href);
    }
}

export function clearUrlParams(url) {
    if (url.searchParams.has('filters')) url.searchParams.delete('filters');
    if (url.searchParams.has('search')) url.searchParams.delete('search');

    history.pushState({}, "", url.href);
}

export function debounce(func, delay) {
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