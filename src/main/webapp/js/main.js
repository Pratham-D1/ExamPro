/**
 * main.js  –  ExamPro Global JavaScript
 * ═══════════════════════════════════════════════════════════
 * This file contains small, focused UI enhancements:
 *  1. Bootstrap 5 form validation
 *  2. Login form: password visibility toggle
 *  3. Login form: loading spinner on submit
 *  4. Smooth scroll for anchor links
 *  5. Navbar transparency effect on scroll
 *
 * All code is wrapped in DOMContentLoaded so it runs only
 * after the HTML is fully parsed.
 */
'use strict';

document.addEventListener('DOMContentLoaded', function () {

    /* ══════════════════════════════════════════════════════
       1. BOOTSTRAP 5 FORM VALIDATION
       Applies Bootstrap's built-in validation classes when
       the user submits an invalid form.
    ══════════════════════════════════════════════════════ */
    var forms = document.querySelectorAll('form[novalidate]');
    forms.forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();   // Stop form submission
                event.stopPropagation();  // Don't bubble the event
            }
            form.classList.add('was-validated'); // Show validation feedback
        });
    });


    /* ══════════════════════════════════════════════════════
       2. PASSWORD VISIBILITY TOGGLE
       Clicking the eye icon in the login form switches the
       password field between "password" and "text" type.
    ══════════════════════════════════════════════════════ */
    var toggleBtn = document.getElementById('togglePassword');
    var passwordInput = document.getElementById('password');
    var toggleIcon = document.getElementById('toggleIcon');

    if (toggleBtn && passwordInput && toggleIcon) {
        toggleBtn.addEventListener('click', function () {
            // Toggle between 'password' and 'text' type
            var isPassword = (passwordInput.type === 'password');
            passwordInput.type = isPassword ? 'text' : 'password';

            // Swap the eye icon
            toggleIcon.className = isPassword ? 'bi bi-eye-slash' : 'bi bi-eye';

            // Accessibility: update aria-label
            toggleBtn.setAttribute(
                'aria-label',
                isPassword ? 'Hide password' : 'Show password'
            );
        });
    }


    /* ══════════════════════════════════════════════════════
       3. LOGIN BUTTON: LOADING SPINNER
       When the login form is submitted (and is valid),
       the button text is replaced with a spinner to give
       the user visual feedback while the server processes.
    ══════════════════════════════════════════════════════ */
    var loginForm = document.getElementById('loginForm');
    var loginBtn  = document.getElementById('btn-login');

    if (loginForm && loginBtn) {
        loginForm.addEventListener('submit', function () {
            // Only show spinner if the form is valid (Bootstrap check)
            if (loginForm.checkValidity()) {
                var btnText    = loginBtn.querySelector('.btn-text');
                var btnSpinner = loginBtn.querySelector('.btn-spinner');

                if (btnText && btnSpinner) {
                    btnText.classList.add('d-none');
                    btnSpinner.classList.remove('d-none');
                    loginBtn.disabled = true;
                }
            }
        });
    }


    /* ══════════════════════════════════════════════════════
       4. SMOOTH SCROLL FOR ANCHOR LINKS
       Clicking a link like <a href="#features"> scrolls
       smoothly instead of jumping instantly.
    ══════════════════════════════════════════════════════ */
    document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (event) {
            var targetId = this.getAttribute('href');
            if (targetId === '#') return; // Skip empty anchors

            var target = document.querySelector(targetId);
            if (target) {
                event.preventDefault();
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });


    /* ══════════════════════════════════════════════════════
       5. NAVBAR SCROLL EFFECT
       On the landing page, the navbar becomes more opaque
       when the user scrolls down, improving readability.
    ══════════════════════════════════════════════════════ */
    var navbar = document.querySelector('.ep-navbar');
    if (navbar) {
        window.addEventListener('scroll', function () {
            if (window.scrollY > 60) {
                navbar.style.background = 'rgba(13, 17, 23, 0.97)';
                navbar.style.boxShadow  = '0 1px 0 rgba(255,255,255,0.05)';
            } else {
                navbar.style.background = 'rgba(13, 17, 23, 0.85)';
                navbar.style.boxShadow  = 'none';
            }
        });
    }

}); // end DOMContentLoaded
