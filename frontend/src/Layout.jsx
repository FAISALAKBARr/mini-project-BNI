import { useState, useEffect } from "react";
import { Link, Outlet, useNavigate } from "react-router-dom";

function Layout({ user, onLogout }) {
    const [menus, setMenus] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetch('http://localhost:8080/api/menus/me', {
            headers: {
                Authorization: `Bearer ${user.token}`,
            },
        })
            .then((res) => {
                if (!res.ok) {
                    throw new Error('Token tidak valid atau kadaluarsa');
                }
                return res.json();
            })
            .then((data) => {
                setMenus(data);
                setLoading(false);
            })
            .catch(() => {
                navigate('/login');
            });
    }, [user.token, navigate]);

    if (loading) return <p>Memuat menu...</p>;

    const topLevelMenus = menus.filter((m) => m.parentId === null);
    const getSubMenus = (parentId) => menus.filter((m) => m.parentId === parentId);

    return (
        <div style={{ display: 'flex', minHeight: '100vh', textAlign: 'left' }}>
            <aside style={{ width: '240px', padding: '16px', borderRight: '1px solid #ccc' }}>
                <h3>Mini Project BNI</h3>
                <p>{user.username} ({user.role})</p>

                <nav>
                    <ul>
                        {/* Dashboard = halaman utama, ditulis manual (bukan dari database) */}
                        <li><Link to="/dashboard">Dashboard</Link></li>

                        {topLevelMenus.map((menu) => {
                            const subMenus = getSubMenus(menu.id);
                            return (
                                <li key={menu.id}>
                                    {subMenus.length > 0 ? (
                                        <>
                                            {/* punya sub-menu -> cuma jadi judul grup, nggak diklik */}
                                            {menu.name}
                                            <ul>
                                                {subMenus.map((sub) => (
                                                    <li key={sub.id}>
                                                        <Link to={sub.path}>{sub.name}</Link>
                                                    </li>
                                                ))}
                                            </ul>
                                        </>
                                    ) : (
                                        <Link to={menu.path}>{menu.name}</Link>
                                    )}
                                </li>
                            );
                        })}
                    </ul>
                </nav>

                <button onClick={onLogout}>Logout</button>
            </aside>

            <main style={{ flex: 1, padding: '16px' }}>
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;