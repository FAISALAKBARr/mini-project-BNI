function Dashboard({ user }) {
    return (
        <div>
            <h2>Dashboard</h2>
            <p>Selamat datang, {user.username} ({user.role})</p>
            <p>Pilih menu di sebelah kiri untuk mulai.</p>
        </div>
    );
}

export default Dashboard;