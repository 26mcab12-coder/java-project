/**
 * Smart Food Mess Management System
 * Client-side validation for the registration form.
 */
document.addEventListener('DOMContentLoaded', function () {
    'use strict';

    var form = document.getElementById('registerForm');
    if (!form) {
        return;
    }

    var nameInput = document.getElementById('name');
    var emailInput = document.getElementById('email');
    var passwordInput = document.getElementById('password');
    var confirmInput = document.getElementById('confirmPassword');

    var nameError = document.getElementById('nameError');
    var emailError = document.getElementById('emailError');
    var passwordError = document.getElementById('passwordError');
    var confirmError = document.getElementById('confirmError');

    var EMAIL_PATTERN = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    function validateName() {
        var name = nameInput.value.trim();
        if (name === '') {
            nameError.textContent = 'Name is required.';
            nameInput.classList.add('invalid');
            return false;
        }
        if (name.length < 3) {
            nameError.textContent = 'Name must be at least 3 characters long.';
            nameInput.classList.add('invalid');
            return false;
        }
        nameError.textContent = '';
        nameInput.classList.remove('invalid');
        return true;
    }

    function validateEmail() {
        var email = emailInput.value.trim();
        if (email === '') {
            emailError.textContent = 'Email is required.';
            emailInput.classList.add('invalid');
            return false;
        }
        if (!EMAIL_PATTERN.test(email)) {
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
        if (password.length < 6) {
            passwordError.textContent = 'Password must be at least 6 characters long.';
            passwordInput.classList.add('invalid');
            return false;
        }
        passwordError.textContent = '';
        passwordInput.classList.remove('invalid');
        return true;
    }

    function validateConfirm() {
        var password = passwordInput.value;
        var confirm = confirmInput.value;
        if (confirm === '') {
            confirmError.textContent = 'Please confirm your password.';
            confirmInput.classList.add('invalid');
            return false;
        }
        if (password !== confirm) {
            confirmError.textContent = 'Passwords do not match.';
            confirmInput.classList.add('invalid');
            return false;
        }
        confirmError.textContent = '';
        confirmInput.classList.remove('invalid');
        return true;
    }

    nameInput.addEventListener('input', validateName);
    emailInput.addEventListener('input', validateEmail);
    passwordInput.addEventListener('input', function () {
        validatePassword();
        if (confirmInput.value !== '') {
            validateConfirm();
        }
    });
    confirmInput.addEventListener('input', validateConfirm);

    form.addEventListener('submit', function (event) {
        var valid = validateName() & validateEmail() & validatePassword() & validateConfirm();
        if (!valid) {
            event.preventDefault();
        }
    });
});