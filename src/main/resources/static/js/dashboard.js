/**
 * ParkWatch Dashboard JavaScript
 * Public Parking Availability Dashboard
 * Student: Sajiya Shaikh | Roll: 24102A2005
 */

'use strict';

/**
 * Initialize the dashboard with auto-refresh and UI enhancements.
 * @param {number} refreshSeconds - Auto-refresh interval in seconds
 */
function initDashboard(refreshSeconds) {
    // Set the last-refreshed time display
    updateRefreshTime();

    // Start auto-refresh countdown
    if (refreshSeconds && refreshSeconds > 0) {
        startAutoRefresh(refreshSeconds);
    }

    // Highlight active nav link
    highlightActiveNav();
}

/**
 * Update the "last refreshed" timestamp label.
 */
function updateRefreshTime() {
    const el = document.getElementById('refresh-time');
    if (el) {
        const now = new Date();
        el.textContent = now.toLocaleTimeString('en-IN', {
            hour: '2-digit', minute: '2-digit', second: '2-digit'
        });
    }
}

/**
 * Start auto-refresh countdown timer.
 * @param {number} seconds
 */
function startAutoRefresh(seconds) {
    let remaining = seconds;
    const btn = document.getElementById('refresh-btn');

    const interval = setInterval(() => {
        remaining--;
        if (btn) {
            btn.title = `Auto-refreshing in ${remaining}s`;
        }
        if (remaining <= 0) {
            clearInterval(interval);
            // Show subtle refresh indication
            if (btn) {
                btn.innerHTML = '<i class="bi bi-arrow-clockwise me-1 spin"></i>Refreshing...';
                btn.disabled = true;
            }
            setTimeout(() => location.reload(), 300);
        }
    }, 1000);
}

/**
 * Highlight the active navigation link based on current URL.
 */
function highlightActiveNav() {
    const path = window.location.pathname;
    const navLinks = {
        'nav-dashboard': ['/', '/dashboard'],
        'nav-alerts':    ['/alerts'],
        'nav-events':    ['/events'],
        'nav-locations': ['/locations']
    };

    Object.entries(navLinks).forEach(([id, paths]) => {
        const el = document.getElementById(id);
        if (el) {
            const isActive = paths.some(p => path === p || path.startsWith(p + '/'));
            if (isActive) {
                el.classList.add('active');
                el.setAttribute('aria-current', 'page');
            }
        }
    });
}

/**
 * Fetch dashboard summary via the REST API and update card values without a full page reload.
 * Optional enhancement – called separately if needed.
 */
async function fetchAndUpdateSummary() {
    try {
        const response = await fetch('/api/v1/dashboard/summary');
        if (!response.ok) throw new Error('API error');
        const data = await response.json();

        setCardText('card-total-locations', '.stat-value', data.totalLocations);
        setCardText('card-total-capacity',  '.stat-value', data.totalCapacity);
        setCardText('card-available',       '.stat-value', data.totalAvailable);
        setCardText('card-occupied',        '.stat-value', data.totalOccupied);
        setCardText('card-utilization',     '.stat-value', data.overallUtilizationPercentage.toFixed(1) + '%');
    } catch (e) {
        console.warn('Failed to fetch summary:', e);
    }
}

function setCardText(cardId, selector, value) {
    const card = document.getElementById(cardId);
    if (card) {
        const el = card.querySelector(selector);
        if (el) el.textContent = value;
    }
}

// Run on DOM ready
document.addEventListener('DOMContentLoaded', function () {
    // Activate Bootstrap tooltips
    const tooltipEls = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipEls.forEach(el => new bootstrap.Tooltip(el));

    // Auto-dismiss alerts after 5 seconds
    document.querySelectorAll('.alert.alert-success').forEach(el => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(el);
            if (bsAlert) bsAlert.close();
        }, 5000);
    });
});
