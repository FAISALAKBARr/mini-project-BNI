import {useState, useEffect} from "react";
import AddUserForm from "./AddUserForm.jsx";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Table, TableBody, TableCell, TableHead, TableHeader, TableRow} from "@/components/ui/table.jsx";
import {
    AlertDialog, AlertDialogAction, AlertDialogCancel,
    AlertDialogContent, AlertDialogDescription, AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
    AlertDialogTrigger
} from "@/components/ui/alert-dialog.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Badge} from "@/components/ui/badge.jsx";

function UserList({ user }) {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');

    useEffect(() => {
        fetch('http://localhost:8080/api/users', {
            headers: {
                Authorization: `Bearer ${user.token}`,
            },
        })
            .then((res) => {
                if (res.status === 403) {
                    throw new Error('Kamu tidak punya akses ke halaman ini (atau sesi login sudah berakhir).');
                }
                if (!res.ok) {
                    throw new Error('Gagal memuat data user.');
                }
                return res.json();
            })
            .then((data) => {
                setUsers(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [user.token]);

    const handleCreated = (created) => {
        setUsers((prev) => [...prev, created]);
    };

    const handleDelete = async (target) => {
        if (!window.confirm(`Hapus user "${target.username}"?`)) return;

        setActionError('');
        try {
            const response = await fetch(`http://localhost:8080/api/users/${target.id}`,{
                method: 'DELETE',
                headers: { Authorization: `Bearer ${user.token}` },
            });

            if (!response.ok) {
                const errorText = await response.text();
                setActionError(errorText || 'Gagal menghapus user');
                return;
            }

            setUsers((prev) => prev.filter((u) => u.id !== target.id));
        } catch (err) {
            setActionError('Tidak bisa terhubung ke server');
        }
    };

    if (loading) return <p className="text-sm text-muted-foreground">Memuat data user...</p>;
    if (error) return <p className="text-sm text-destructive">{error}</p>;

    return (
        <div className="flex flex-col gap-6">
            <Card>
                <CardHeader>
                    <CardTitle>Tambah User</CardTitle>
                </CardHeader>
                <CardContent>
                    <AddUserForm token={user.token} onCreated={handleCreated} />
                </CardContent>
            </Card>

            {actionError && <p className="text-sm text-destructive">{actionError}</p>}

            <Card>
                <CardHeader>
                    <CardTitle>Data User</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>Username</TableHead>
                                <TableHead>Role</TableHead>
                                <TableHead className="text-right">Aksi</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {users.map((u) => (
                                <TableRow key={u.id}>
                                    <TableCell>{u.id}</TableCell>
                                    <TableCell>{u.username}</TableCell>
                                    <TableCell>
                                        <Badge variant="secondary">{u.role}</Badge>
                                    </TableCell>
                                    <TableCell className="text-right">
                                        {u.username === user.username ? (
                                            <span className="text-sm text-muted-foreground italic">
                                                (sedang login)
                                            </span>
                                        ) : (
                                            <AlertDialog>
                                                <AlertDialogTrigger asChild>
                                                    <Button variant="destructive" size="sm">
                                                        Hapus
                                                    </Button>
                                                </AlertDialogTrigger>
                                                <AlertDialogContent>
                                                    <AlertDialogHeader>
                                                        <AlertDialogTitle>
                                                            Hapus user "{u.username}"?
                                                        </AlertDialogTitle>
                                                        <AlertDialogDescription>
                                                            Tindakan ini tidak bisa dibatalkan. User akan dihapus permanen dari database.
                                                        </AlertDialogDescription>
                                                    </AlertDialogHeader>
                                                    <AlertDialogFooter>
                                                        <AlertDialogCancel>Batal</AlertDialogCancel>
                                                        <AlertDialogAction onClick={() => handleDelete(u)}>
                                                            Hapus
                                                        </AlertDialogAction>
                                                    </AlertDialogFooter>
                                                </AlertDialogContent>
                                            </AlertDialog>
                                        )}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
}

export default UserList;

// Yang baru di sini:
// Tiga kondisi tampilan: loading, error, dan sukses. Ada state error baru, dan dua baris if (...) return di atas menentukan apa yang tampil.
// Kenapa 403 nggak di-redirect ke /login seperti di Layout: user staff itu sudah login dengan benar, dia cuma nggak berhak. Melempar dia ke halaman login justru membingungkan, jadi kita tampilkan pesan.
// .map() + key untuk baris tabel, pola yang sama seperti menu. Variabel barisnya u, bukan user, supaya nggak bentrok dengan prop user (yang login).
// cellPadding ditulis camelCase karena aturan JSX. border dan cellPadding ini styling sementara sampai tahap styling nanti.

//Konsep React yang baru di sini:
//
// <select> terkontrol: pola sama seperti <input> (value + onChange). Di React, value dipasang di <select>, bukan selected di <option>.
// try / catch / finally: finally selalu jalan, termasuk saat ada return di dalam try. Ini menjamin tombol tidak nyangkut di "Menyimpan...", dan disabled={submitting} mencegah klik ganda.
// Update state tanpa mengubah yang lama (immutability): [...prev, created] dan prev.filter(...) menghasilkan array baru. React mendeteksi perubahan dari referensi baru, jadi prev.push(...) tidak akan memicu render ulang. Bentuk (prev) => ... memakai nilai state paling mutakhir.
// Anak memberi kabar ke induk lewat props fungsi (onCreated): sama seperti onLoginSuccess di Login. AddUserForm tidak perlu tahu isi tabel.
// Tombol Hapus disembunyikan untuk akun sendiri: itu cuma kemudahan UI. Backend tetap menolak, prinsip yang sama dengan menyembunyikan menu.