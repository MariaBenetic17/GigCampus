// GigCampus Full-Stack JavaScript Utilities & Real-Time Client
document.addEventListener('DOMContentLoaded', function () {
    // 1. Auto-dismiss alerts after 6 seconds
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            try {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            } catch (e) {}
        }, 6000);
    });

    // 2. Form confirmation for destructive actions
    const confirmButtons = document.querySelectorAll('[data-confirm]');
    confirmButtons.forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            const message = btn.getAttribute('data-confirm') || 'Are you sure you want to proceed?';
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // 3. Sync budget filter label if slider is used
    const budgetInput = document.getElementById('maxBudgetInput');
    const budgetDisplay = document.getElementById('budgetDisplay');
    if (budgetInput && budgetDisplay) {
        budgetInput.addEventListener('input', function () {
            budgetDisplay.textContent = '₹' + Number(budgetInput.value).toLocaleString('en-IN');
        });
    }

    // 4. Real-Time In-App Live Notifications (WebSocket / STOMP)
    const authUserId = document.getElementById('authUserId')?.value;
    const authUserEmail = document.getElementById('authUserEmail')?.value;
    const badgeEl = document.getElementById('globalNotificationBadge');
    const toastEl = document.getElementById('liveNotificationToast');

    if (authUserId && typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        let notifStompClient = null;

        function connectNotifications() {
            try {
                const socket = new SockJS('/ws');
                notifStompClient = Stomp.over(socket);
                notifStompClient.debug = null; // Quiet mode

                notifStompClient.connect({}, function (frame) {
                    // Subscribe to user's private notification topic
                    const topic = '/topic/notifications.' + authUserId;
                    notifStompClient.subscribe(topic, function (msg) {
                        try {
                            const notif = JSON.parse(msg.body);
                            handleIncomingNotification(notif);
                        } catch (err) {
                            console.error('Error parsing live notification:', err);
                        }
                    });
                }, function (err) {
                    // Reconnect fallback after 8 seconds
                    setTimeout(connectNotifications, 8000);
                });
            } catch (e) {
                console.warn('Live notification socket notice:', e);
            }
        }

        connectNotifications();

        function handleIncomingNotification(notif) {
            // Update badge count
            if (badgeEl) {
                const current = parseInt(badgeEl.textContent.trim(), 10) || 0;
                const newCount = notif.unreadCount ? notif.unreadCount : (current + 1);
                badgeEl.textContent = newCount;
                badgeEl.style.display = 'inline-block';
                badgeEl.classList.add('animate__animated', 'animate__pulse');
            }

            // Show Toast Alert
            if (toastEl && typeof bootstrap !== 'undefined') {
                const titleEl = document.getElementById('toastTitle');
                const msgEl = document.getElementById('toastMessage');
                const linkContainer = document.getElementById('toastLinkContainer');
                const linkEl = document.getElementById('toastLink');

                if (titleEl) titleEl.textContent = notif.title || 'New Notification';
                if (msgEl) msgEl.textContent = notif.message || '';

                if (linkContainer && linkEl && notif.linkUrl) {
                    linkEl.href = notif.linkUrl;
                    linkContainer.style.display = 'block';
                } else if (linkContainer) {
                    linkContainer.style.display = 'none';
                }

                const bsToast = new bootstrap.Toast(toastEl, { delay: 6000 });
                bsToast.show();
            }
        }

        // Periodic background poll fallback (every 60s)
        setInterval(function () {
            fetch('/api/notifications/unread-count')
                .then(r => r.json())
                .then(data => {
                    if (badgeEl && data.unreadCount !== undefined) {
                        if (data.unreadCount > 0) {
                            badgeEl.textContent = data.unreadCount;
                            badgeEl.style.display = 'inline-block';
                        } else {
                            badgeEl.style.display = 'none';
                        }
                    }
                })
                .catch(() => {});
        }, 60000);
    }
});
