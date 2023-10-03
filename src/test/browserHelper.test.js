/**
 * @jest-environment jsdom
 */

import {jest, test, describe, expect, beforeEach, afterEach} from '@jest/globals';
import {clearUrlParams, debounce, updateUrlParam} from '../main/resources/assets/js/browserHelper.js';

describe('debounce', () => {

    test('Function is called after delay', () => {
        jest.useFakeTimers(); // Use Jest's fake timers

        const mockFunction = jest.fn();
        const debouncedFunction = debounce(mockFunction, 1000);

        // Call the debounced function
        debouncedFunction();

        // Fast-forward time by 1000 milliseconds (1 second)
        jest.advanceTimersByTime(1000);

        // Expect the function to have been called
        expect(mockFunction).toHaveBeenCalled();
    });

    test('Function is not called immediately', () => {
        jest.useFakeTimers();

        const mockFunction = jest.fn();
        const debouncedFunction = debounce(mockFunction, 1000);

        // Call the debounced function
        debouncedFunction();

        // The function should not have been called yet
        expect(mockFunction).not.toHaveBeenCalled();

        // Fast-forward time by less than 1000 milliseconds
        jest.advanceTimersByTime(500);

        // The function should still not have been called
        expect(mockFunction).not.toHaveBeenCalled();

        // Fast-forward time by another 500 milliseconds
        jest.advanceTimersByTime(500);

        // Now, the function should have been called
        expect(mockFunction).toHaveBeenCalled();
    });

    test('Function is called with correct arguments', () => {
        jest.useFakeTimers();

        const mockFunction = jest.fn();
        const debouncedFunction = debounce(mockFunction, 1000);

        // Call the debounced function with arguments
        debouncedFunction(42, 'test');

        // Fast-forward time by 1000 milliseconds (1 second)
        jest.advanceTimersByTime(1000);

        // Expect the function to have been called with the correct arguments
        expect(mockFunction).toHaveBeenCalledWith(42, 'test');
    });
});

describe('clearUrlParams', () => {

    beforeEach(() => {
        window = Object.create(window);
        window.testCtx = {
            /**
             * Allows for setting `window.location` props within tests
             * @param {String} prop - The `location` prop you want to set.
             * @param {String} val - The value of the prop.
             */
            location: function(prop, val){
                Object.defineProperty(window, 'location', {
                    value: {
                        reload: jest.fn()
                    },
                    writable: true
                });
            }
        };

        // mock out pushState to avoid SecurityError's
        jest.spyOn(window.history, 'pushState');
        window.history.pushState.mockImplementation((state, title, url) => {
            window.testCtx.location('href', url);
        });
    });

    afterEach(() => {
        jest.clearAllMocks();
        window.history.pushState.mockRestore()
    });

    test('should remove "filters" and "search" parameters from the URL', () => {
        const url = new URL('http://example.com/?filters=1&search=test');
        jest.spyOn(url.searchParams, 'delete');

        clearUrlParams(url);

        // Ensure that searchParams.delete was called for both parameters
        expect(url.searchParams.delete).toHaveBeenCalledWith('filters');
        expect(url.searchParams.delete).toHaveBeenCalledWith('search');

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/');
    });

    test('should not remove parameters if they do not exist in the URL', () => {
        const url = new URL('http://example.com/?some=other');
        jest.spyOn(url.searchParams, 'delete');

        clearUrlParams(url);

        // Ensure that searchParams.delete was not called since there are no matching parameters
        expect(url.searchParams.delete).not.toHaveBeenCalled();

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/?some=other');
    });
});

describe('updateUrlParams', () => {
    beforeEach(() => {
        // mock out pushState to avoid SecurityError's
        jest.spyOn(window.history, 'pushState');
        window.history.pushState.mockImplementation((state, title, url) => {});
    });

    afterEach(() => {
        window.history.pushState.mockRestore()
        jest.clearAllMocks();
    });

    test('should add "filter" URL parameter with new values', () => {
        const url = new URL('http://example.com/');
        jest.spyOn(url.searchParams, 'set');

        updateUrlParam(url, 'filters', 'critical');

        // Ensure that searchParams.set was called
        expect(url.searchParams.set).toHaveBeenCalledWith('filters', 'critical');

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/?filters=critical');
    });

    test('should update "filter" URL parameter with new values', () => {
        const url = new URL('http://example.com/?filters=critical');
        jest.spyOn(url.searchParams, 'set');

        updateUrlParam(url, 'filters', 'critical,serious');

        // Ensure that searchParams.set was called
        expect(url.searchParams.set).toHaveBeenCalledWith('filters', 'critical,serious');

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/?filters=critical%2Cserious');
    });

    test('should delete "filter" URL parameter when value is empty', () => {
        const url = new URL('http://example.com/?filters=critical');
        jest.spyOn(url.searchParams, 'delete');

        updateUrlParam(url, 'filters', '');

        // Ensure that searchParams.set was called
        expect(url.searchParams.delete).toHaveBeenCalledWith('filters');

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/');
    });

    test('should delete "filter" URL parameter when value is undefined', () => {
        const url = new URL('http://example.com/?filters=critical');
        jest.spyOn(url.searchParams, 'delete');

        updateUrlParam(url, 'filters');

        // Ensure that searchParams.set was called
        expect(url.searchParams.delete).toHaveBeenCalledWith('filters');

        // Ensure that history.pushState was called with the correct arguments
        expect(history.pushState).toHaveBeenCalledWith({}, '', 'http://example.com/');
    });
});

