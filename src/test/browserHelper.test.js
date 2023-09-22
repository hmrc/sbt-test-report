/**
 * @jest-environment jsdom
 */

const {test, describe, expect} = require('@jest/globals');
const {debounce} = require("../main/resources/assets/js/browserHelper.mjs");

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