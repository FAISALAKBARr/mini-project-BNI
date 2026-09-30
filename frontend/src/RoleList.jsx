import {useEffect, useState} from "react";
import AddRoleForm from "./AddRoleForm.jsx";
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

function RoleList({ user }) {
    const [roles, setRoles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [actionError, setActionError] = useState('');

    useEffect(() => {
        fetch('http://localhost:8080/api/roles', {
            headers: {Authorization: `Bearer ${user.token}`},
        })
            .then((res) => {
                if (!res.ok) throw new Error('Gagal memuat data role.');
                return res.json();
            })
            .then((data) => {
                setRoles(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [user.token]);

    const handleCreated = (created) => {
        setRoles((prev) => [...prev, created]);
    };

    const handleDelete = async (target) => {
        if (!window.confirm(`Hapus role "${target.name}"?`)) return;

        setActionError('');
        try{
            const response = await fetch(`http://localhost:8080/api/roles/${target.id}`, {
                method: 'DELETE',
                headers: { Authorization: `Bearer ${user.token}`},
            });

            if (!response.ok) {
                const errorText = await response.text();
                setActionError(errorText || 'Gagal menghapus role');
                return;
            }

            setRoles((prev) => prev.filter((r) => r.id !== target.id));
        } catch (err) {
            setActionError('Tidak bisa terhubung ke server');
        }
    };

    if (loading) return <p className="text-sm text-muted-foreground">Memuat data role...</p>;
    if (error) return <p className="text-sm text-destructive">{error}</p>;

    return (
        <div className="flex flex-col gap-6">
            <Card>
                <CardHeader>
                    <CardTitle>Tambah Role</CardTitle>
                </CardHeader>
                <CardContent>
                    <AddRoleForm token={user.token} onCreated={handleCreated} />
                </CardContent>
            </Card>

            {actionError && <p className="text-sm text-destructive">{actionError}</p>}

            <Card>
                <CardHeader>
                    <CardTitle>Data Role</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>ID</TableHead>
                                <TableHead>Nama Role</TableHead>
                                <TableHead className="text-right">Aksi</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {roles.map((r) => (
                                <TableRow key={r.id}>
                                    <TableCell>{r.id}</TableCell>
                                    <TableCell>{r.name}</TableCell>
                                    <TableCell className="text-right">
                                        <AlertDialog>
                                            <AlertDialogTrigger asChild>
                                                <Button variant="destructive" size="sm">
                                                    Hapus
                                                </Button>
                                            </AlertDialogTrigger>
                                            <AlertDialogContent>
                                                <AlertDialogHeader>
                                                    <AlertDialogTitle>
                                                        Hapus role "{r.name}"?
                                                    </AlertDialogTitle>
                                                    <AlertDialogDescription>
                                                        Role tidak bisa dihapus kalau masih dipakai oleh user atau menu.
                                                    </AlertDialogDescription>
                                                </AlertDialogHeader>
                                                <AlertDialogFooter>
                                                    <AlertDialogCancel>Batal</AlertDialogCancel>
                                                    <AlertDialogAction onClick={() => handleDelete(r)}>
                                                        Hapus
                                                    </AlertDialogAction>
                                                </AlertDialogFooter>
                                            </AlertDialogContent>
                                        </AlertDialog>
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

export default RoleList;