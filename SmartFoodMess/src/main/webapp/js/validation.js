/**
 * Smart Food Mess Management System
 * Client-side validation for the login form.
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    var form = document.getElementById('loginForm');
    var emailInput = document.getElementById('email');
    var passwordInput = document.getElementById('password');
    var emailError = document.getElementById('emailError');
    var passwordError = document.getElementById('passwordError');
    if (!form) {
        return;
    }

    var EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    function isValidEmail(value) {
        return EMAIL_PATTERN.test(value);
    }

    function validateEmail() {
        var email = emailInput.value.trim();

        if (email === '') {
            emailError.textContent = 'Email is required.';
            emailInput.classList.add('invalid');
            return false;
        }

        if (!isValidEmail(email)) {
            emailError.textContent = 'Please enter a valid email address (e.g., name@domain.com).';
            emailInput.classList.add('invalid');
            return false;
        }

        emailError.textContent = '';
        emailInput.classList.remove('invalid');
        return true;
    }

    function validatePassword() {
        var password = passwordInput.value;

        if (password === '') {
            passwordError.textContent = 'Password is required.';
            passwordInput.classList.add('invalid');
            return false;
        }

        passwordError.textContent = '';
        passwordInput.classList.remove('invalid');
        return true;
    }

    emailInput.addEventListener('input', validateEmail);
    passwordInput.addEventListener('input', validatePassword);

    form.addEventListener('submit', function (event) {
        var isEmailValid = validateEmail();
        var isPasswordValid = validatePassword();

        if (!isEmailValid || !isPasswordValid) {
            event.preventDefault();
        }
    });
});