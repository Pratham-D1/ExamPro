<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="dropdown me-2" id="epNotifDropdownWrapper">
    <button class="btn btn-outline-secondary btn-sm position-relative text-white border-secondary"
            type="button" id="epNotifBellBtn" data-bs-toggle="dropdown" data-bs-auto-close="outside" aria-expanded="false" title="Notifications">
        <i class="bi bi-bell-fill"></i>
        <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger d-none" id="epNotifBadge">
            0
        </span>
    </button>
    <div class="dropdown-menu dropdown-menu-end dropdown-menu-dark shadow-lg p-0 border-secondary"
         style="width: 340px; max-width: 92vw; background: #161b22;" aria-labelledby="epNotifBellBtn">
        <div class="d-flex justify-content-between align-items-center px-3 py-2 border-bottom border-secondary bg-dark bg-opacity-50">
            <span class="fw-bold small text-white"><i class="bi bi-bell me-1"></i> Notifications</span>
            <button class="btn btn-link btn-sm text-info p-0 text-decoration-none small" id="epMarkAllReadBtn" type="button">
                Mark all read
            </button>
        </div>
        <div class="list-group list-group-flush overflow-auto" style="max-height: 290px;" id="epNotifList">
            <div class="p-3 text-center text-secondary small">
                <div class="spinner-border spinner-border-sm text-secondary me-2" role="status"></div>
                Checking alerts...
            </div>
        </div>
        <div class="p-2 border-top border-secondary text-center bg-dark bg-opacity-25">
            <a href="${pageContext.request.contextPath}/notifications" class="text-decoration-none small text-info fw-semibold">
                Open Notification Center <i class="bi bi-arrow-right ms-1"></i>
            </a>
        </div>
    </div>
</div>

<script>
(function() {
    const ctx = '${pageContext.request.contextPath}';
    const csrfToken = '${csrfToken}';
    const badge = document.getElementById('epNotifBadge');
    const list = document.getElementById('epNotifList');
    const markAllBtn = document.getElementById('epMarkAllReadBtn');

    function fetchNotifications() {
        fetch(ctx + '/notifications/api?action=poll', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(res => res.json())
        .then(data => {
            if (!data || !data.success) return;
            if (badge) {
                if (data.unreadCount > 0) {
                    badge.textContent = data.unreadCount > 99 ? '99+' : data.unreadCount;
                    badge.classList.remove('d-none');
                } else {
                    badge.classList.add('d-none');
                }
            }
            if (list) {
                if (!data.notifications || data.notifications.length === 0) {
                    list.innerHTML = '<div class="p-4 text-center text-secondary small"><i class="bi bi-check2-all fs-4 d-block mb-1 text-success"></i>All caught up! No new alerts.</div>';
                    return;
                }
                let html = '';
                data.notifications.forEach(function(n) {
                    const unreadClass = n.read ? '' : 'bg-dark bg-opacity-50 border-start border-3 border-info';
                    const linkUrl = n.actionUrl ? (ctx + n.actionUrl) : (ctx + '/notifications');
                    html += '<a href="' + linkUrl + '" class="list-group-item list-group-item-action list-group-item-dark px-3 py-2 border-secondary ' + unreadClass + '">' +
                                '<div class="d-flex align-items-start gap-2">' +
                                    '<span class="badge ' + (n.badgeClass || 'bg-secondary') + ' p-1 mt-1"><i class="bi ' + (n.categoryIcon || 'bi-bell') + '"></i></span>' +
                                    '<div class="flex-grow-1 overflow-hidden">' +
                                        '<div class="d-flex justify-content-between align-items-center">' +
                                            '<div class="fw-semibold text-white small text-truncate" style="max-width: 190px;">' + escapeHtml(n.title) + '</div>' +
                                            '<span class="text-secondary" style="font-size: 0.7rem;">' + (n.timeAgo || '') + '</span>' +
                                        '</div>' +
                                        '<div class="text-secondary small text-truncate" style="font-size: 0.78rem;">' + escapeHtml(n.message) + '</div>' +
                                    '</div>' +
                                '</div>' +
                            '</a>';
                });
                list.innerHTML = html;
            }
        })
        .catch(err => console.debug('Notif poll error', err));
    }

    if (markAllBtn) {
        markAllBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const form = new URLSearchParams();
            form.append('action', 'markAllRead');
            form.append('csrfToken', csrfToken);

            fetch(ctx + '/notifications', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: form
            })
            .then(res => res.json())
            .then(data => {
                if (data && data.success) {
                    if (badge) badge.classList.add('d-none');
                    fetchNotifications();
                }
            })
            .catch(err => console.error(err));
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    document.addEventListener('DOMContentLoaded', fetchNotifications);
})();
</script>

