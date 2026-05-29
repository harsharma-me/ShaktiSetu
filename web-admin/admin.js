import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getFirestore, collection, onSnapshot, query, where, getDocs, orderBy, addDoc, serverTimestamp, doc, getDoc } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js";
import { getAuth, signInWithEmailAndPassword, onAuthStateChanged, signOut } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js";

// Your Firebase Config from Firebase Console
const firebaseConfig = {
    apiKey: "AIzaSyDU8vytjT_9wp2nTrF9UUAEmLl-TTTfGno",
    authDomain: "shaktisetu-18b97.firebaseapp.com",
    databaseURL: "https://shaktisetu-18b97-default-rtdb.firebaseio.com",
    projectId: "shaktisetu-18b97",
    storageBucket: "shaktisetu-18b97.firebasestorage.app",
    messagingSenderId: "933736652914",
    appId: "1:933736652914:web:8814e67a9d20002364a2eb",
    measurementId: "G-V76LEK5S57"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const auth = getAuth(app);

// State Management
let allUsers = [];

// --- AUTHENTICATION ---
const loginForm = {
    overlay: document.getElementById('login-overlay'),
    email: document.getElementById('login-email'),
    pass: document.getElementById('login-password'),
    btn: document.getElementById('btn-login'),
    error: document.getElementById('login-error')
};

loginForm.btn.addEventListener('click', async () => {
    const email = loginForm.email.value;
    const pass = loginForm.pass.value;
    loginForm.error.style.display = 'none';

    try {
        await signInWithEmailAndPassword(auth, email, pass);
    } catch (e) {
        loginForm.error.innerText = "Invalid Admin Credentials";
        loginForm.error.style.display = 'block';
    }
});

onAuthStateChanged(auth, async (user) => {
    if (user) {
        // Check if user is admin
        try {
            const adminDoc = await getDoc(doc(db, "admins", user.uid));
            if (adminDoc.exists()) {
                const adminData = adminDoc.data();
                loginForm.overlay.style.display = 'none';
                document.querySelector('.sidebar').style.display = 'flex';
                document.querySelector('main').style.display = 'block';
                document.getElementById('admin-email-display').innerText = `${user.email} (${adminData.role || 'Admin'})`;
                listenToSOS();
            } else {
                alert("Unauthorized: You do not have admin privileges.");
                signOut(auth);
            }
        } catch (e) {
            console.error("Role check failed:", e);
            signOut(auth);
        }
    } else {
        // Logged out
        loginForm.overlay.style.display = 'flex';
        document.querySelector('.sidebar').style.display = 'none';
        document.querySelector('main').style.display = 'none';
    }
});

window.logout = () => {
    signOut(auth);
};

// --- SECTION NAVIGATION ---
window.showSection = (sectionId, element) => {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(s => s.style.display = 'none');

    // Show target section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.style.display = 'block';
    }

    // Update active nav state
    document.querySelectorAll('.sidebar nav li').forEach(li => li.classList.remove('active'));
    if (element) {
        element.classList.add('active');
    }

    const titles = {
        'live-sos': 'Live SOS Monitoring',
        'users': 'User Management',
        'notifications': 'Broadcast Center'
    };
    document.getElementById('section-title').innerText = titles[sectionId] || 'Dashboard';

    if (sectionId === 'users' || sectionId === 'notifications') fetchUsers();
};

// --- LIVE SOS LISTENER ---
let sosUnsubscribe = null;
const listenToSOS = () => {
    if (sosUnsubscribe) sosUnsubscribe();

    const sosList = document.getElementById('sos-list');
    const alertCount = document.getElementById('active-alert-count');

    sosUnsubscribe = onSnapshot(collection(db, "live_sos"), (snapshot) => {
        sosList.innerHTML = '';
        let count = 0;

        if (snapshot.empty) {
            sosList.innerHTML = '<p class="empty-msg">No active emergencies at the moment.</p>';
        }

        snapshot.forEach((doc) => {
            count++;
            const data = doc.data();
            const card = document.createElement('div');
            card.className = 'sos-card';
            card.innerHTML = `
                <h3>🚨 ${data.email || 'Unknown User'}</h3>
                <p><strong>UID:</strong> ${doc.id}</p>
                <div class="evidence-preview">
                    ${data.last_photo ? `<img src="${data.last_photo}" alt="SOS Photo" style="width:100%; border-radius:8px; margin-top:10px;">` : ''}
                    ${data.last_audio ? `<audio controls src="${data.last_audio}" style="width:100%; margin-top:10px;"></audio>` : ''}
                </div>
                <p><strong>Last Updated:</strong> ${data.lastUpdated?.toDate().toLocaleTimeString() || 'N/A'}</p>
                <a href="${data.osm_link}" target="_blank" class="btn-track">Open Live Map</a>
            `;
            sosList.appendChild(card);
        });

        alertCount.innerText = count;
    });
};

// --- USER MANAGEMENT ---
const fetchUsers = async () => {
    const tableBody = document.getElementById('user-table-body');
    const userSelect = document.getElementById('notif-user-select');

    if (tableBody) tableBody.innerHTML = '<tr><td colspan="4">Loading users...</td></tr>';

    try {
        const q = query(collection(db, "users"), orderBy("createdAt", "desc"));
        const snapshot = await getDocs(q);
        allUsers = [];
        if (tableBody) tableBody.innerHTML = '';
        if (userSelect) userSelect.innerHTML = '';

        snapshot.forEach((doc) => {
            const user = doc.data();
            allUsers.push({ id: doc.id, ...user });
            if (tableBody) renderUserRow(user, doc.id);

            if (userSelect) {
                const opt = document.createElement('option');
                opt.value = doc.id;
                opt.text = user.email || doc.id;
                userSelect.appendChild(opt);
            }
        });
    } catch (e) {
        console.error("Error fetching users:", e);
        if (tableBody) tableBody.innerHTML = '<tr><td colspan="4">Error loading users. Check console.</td></tr>';
    }
};

const renderUserRow = (user, id) => {
    const tableBody = document.getElementById('user-table-body');
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${user.email}</td>
        <td><small>${id}</small></td>
        <td><span class="badge">${user.terms_agreed ? 'Verified' : 'Pending'}</span></td>
        <td>
            <button onclick="alert('Sending ping to ${user.email}...')">Ping</button>
        </td>
    `;
    tableBody.appendChild(row);
};

// --- NOTIFICATIONS ---
window.toggleUserSelect = () => {
    const target = document.getElementById('notif-target').value;
    const group = document.getElementById('specific-user-group');
    group.style.display = (target === 'specific') ? 'block' : 'none';
};

window.sendNotification = async () => {
    const title = document.getElementById('notif-title').value;
    const body = document.getElementById('notif-body').value;
    const target = document.getElementById('notif-target').value;
    const specificUid = document.getElementById('notif-user-select').value;

    if (!title || !body) {
        alert("Please fill in the message details!");
        return;
    }

    try {
        const notificationData = {
            title: title,
            body: body,
            target: target,
            timestamp: serverTimestamp()
        };

        if (target === 'specific') {
            notificationData.uid = specificUid;
        }

        await addDoc(collection(db, "notifications"), notificationData);

        alert(`Broadcast sent successfully!`);

        // Clear inputs
        document.getElementById('notif-title').value = '';
        document.getElementById('notif-body').value = '';
    } catch (e) {
        console.error("Error sending notification:", e);
        alert("Error sending notification. Check console.");
    }
};

// Search Logic
document.getElementById('user-search')?.addEventListener('input', (e) => {
    const term = e.target.value.toLowerCase();
    const filtered = allUsers.filter(u => u.email.toLowerCase().includes(term));
    const tableBody = document.getElementById('user-table-body');
    tableBody.innerHTML = '';
    filtered.forEach(u => renderUserRow(u, u.id));
});
