import {useEffect, useState} from "react";
import {
    Dialog,
    DialogContent,
    DialogDescription, DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger
} from "@/components/ui/dialog.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Label} from "@/components/ui/label.jsx";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select.jsx";
import {Input} from "@/components/ui/input.jsx";

function EditUserDialog({ targetUser, currentUsername, token, onUpdated}) {
    const isSelf = targetUser.username === currentUsername;

    const [open, setOpen] = useState(false);
    const [username, setUsername] = useState(targetUser.username);
    const [role, setRole] = useState(targetUser.role);
    const [roles, setRoles] = useState([]);
    const [password, setPassword] = useState('');
    const [message, setMessage] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (open){
            setUsername(targetUser.username);
            setRole(targetUser.role);
            setPassword('');
            setMessage('');
        }
    }, [open, targetUser.username, targetUser.role]);

    // Ambil daftar role cuma pas dialog ini dibuka -- bukan tiap baris tabel ikut fetch sendiri-sendiri
    useEffect(() => {
        if (!open) return;
        fetch('http://localhost:8080/api/roles', {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then((res) => res.json())
            .then((data) => setRoles(data));
    }, [open, token]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setSubmitting(true);

        try {
            const response = await fetch(`http://localhost:8080/api/users/${targetUser.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({username, role, password: password || null}),
            });

            if (!response.ok) {
                const errorText = await response.text();
                setMessage(errorText || 'Gagal mengubah user');
                return;
            }

            const updated = await response.json();
            onUpdated(updated);
            setOpen(false);
        }catch (error){
            setMessage('Tidak bisa terhubung ke server');
        }finally {
            setSubmitting(false);
        }
    };
    return(
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger render={<Button variant="outline" size="sm">Edit</Button>}/>
            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>Edit User</DialogTitle>
                        <DialogDescription>
                            {isSelf
                                ? 'Ini akunmu sendiri. Kalau username diubah, kamu perlu login ulang setelah ini.'
                                : `Ubah data untuk "${targetUser.username}".`}
                        </DialogDescription>
                    </DialogHeader>
                    <div className="flex flex-col gap-4 py-4">
                        <div className="flex flex-col gap-2">
                            <Label htmlFor={`edit-username-${targetUser.id}`}>Username</Label>
                            <Input
                                id={`edit-username-${targetUser.id}`}
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                            />
                        </div>
                        <div className="flex flex-col gap-2">
                            <Label>Role</Label>
                            <Select value={role} onValueChange={setRole} disabled={isSelf}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    {roles.map((r) => (
                                        <SelectItem key={r.id} value={r.name}>{r.name}</SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {isSelf && (
                                <p className="text-xs text-muted-foreground">
                                    Role akun sendiri tidak bisa diubah dari sini.
                                </p>
                            )}
                        </div>
                        <div className="flex flex-col gap-2">
                            <Label htmlFor={`edit-password-${targetUser.id}`}>Password Baru (opsional)</Label>
                            <Input
                                id={`edit-password-${targetUser.id}`}
                                type="password"
                                placeholder="Kosongkan kalau tidak diganti"
                                autoComplete="new-password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                            />
                        </div>
                        {message && <p className="text-sm text-destructive">{message}</p>}
                    </div>
                    <DialogFooter>
                        <Button type="submit" disabled={submitting}>
                            {submitting ? 'Menyimpan...' : 'Simpan'}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    );
}
export default EditUserDialog;

//disabled={isSelf} di Select role itu murni pencegahan di sisi tampilan — mencegah user pilih role lain buat akun sendiri terus kena tolak pas submit. Validasi yang sebenarnya mengikat tetap di backend (UserService), persis seperti validasi lain di project ini.