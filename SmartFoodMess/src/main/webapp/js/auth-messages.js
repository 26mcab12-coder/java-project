(function () {
    'use strict';

    var query = new URLSearchParams(window.location.search);
    var errors = {
        missing: 'Please enter both email and password.',
        invalid: 'Invalid email or password. Please try again.',
        database: 'Login is temporarily unavailable. Check that MySQL is running, the mess_db schema is installed, and database settings are correct.',
        name: 'Name must be at least 3 characters long.',
        email: 'Please enter a valid email address.',
        password: 'Password must be at least 6 characters long.',
        mismatch: 'Passwords do not match.',
        exists: 'An account with this email already exists. Please login instead.',
        failed: 'Registration failed. Please try again.'
    };
    var error = errors[query.get('error')];
    if (error) {
        var errorElement = document.getElementById('errorMessage');
        errorElement.textContent = error;
        errorElement.hidden = false;
    }

    if (query.get('registered') === 'true') {
        var successElement = document.getElementById('successMessage');
        if (successElement) {
            successElement.textContent = 'Registration successful! You can now login with your credentials.';
            successElement.hidden = false;
        }
    }
})();
