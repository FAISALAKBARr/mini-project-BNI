import {useNavigate} from "react-router-dom";
import {useCallback, useEffect, useMemo, useState} from "react";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Button} from "@/components/ui/button.jsx";
import {useNow} from "@/hooks/use-now.js";
import AttendanceMap from "@/AttendanceMap.jsx";

const API = "http://localhost:8080/api/attendance";

// Backend mengirim jam dalam zona kantor ("2026-10-05T08:10:00+07:00"), jadi HH:mm diambil langsung dari teksnya.
const hhmm = (iso) => (iso ? iso.substring(11, 16) : "-");
const formatDuration = (minutes) => `${Math.floor(minutes / 60)} jam ${minutes % 60} menit`;

function LiveClock({ offsetMs}) {
    const now = useNow(offsetMs, 1000);
    return <span>{new Date(now).toLocaleTimeString("id-ID", { timeZone:"Asia/Jakarta", hour12: false })}</span>;
}

function WorkedTime({ record, offsetMs }) {
    const now = useNow(offsetMs, 30000);
    if (!record) return <span>-</span>;
    if (record.workMinutes != null) return <span>
        {formatDuration(record.workMinutes)}
    </span>;
    const minutes = Math.max(0, Math.floor((now - Date.parse(record.countedFromAt)) / 60000));
    return <span>{formatDuration(minutes)} (berjalan)</span>;
}

function Row({ label, children }){
    return (
        <div className="flex justify-between gap-4 text-sm">
            <span className="text-muted-foreground">{label}</span>
            <span className="text-right font-medium">{children}</span>
        </div>
    );
}

function Attendance({ user }) {
    const navigate = useNavigate();
    const { position, error: geoError } = useGeolocation();
    const [status, setStatus] = useState(null);      // jawaban GET /api/attendance/today
    const [offsetMs, setOffsetMs] = useState(0);     // selisih jam server dan jam browser
    const [loadError, setLoadError] = useState("");
    const [message, setMessage] = useState(null);    // { type: "ok" | "error", text }
    const [submitting, setSubmitting] = useState(false);

    const authHeaders = useMemo(() => ({ Authorization: `Bearer ${user.token}` }), [user.token]);

    const loadStatus = useCallback(() => {
        fetch(`${API}/today`, { headers: authHeaders })
            .then((res) => {
                if (res.status === 401) {
                    navigate("/login");
                    throw new Error("Sesi berakhir, silahkan login ulang");
                }
                if (!res.ok) throw new Error("Gagal memuat status absensi");
                return res.json();
            })
            .then((data) => {
                setStatus(data);
                setOffsetMs(Date.parse(data.serverTime) - Date.now());
                setLoadError("");
            })
            .catch((err) => setLoadError(err.message));
    }, [authHeaders, navigate]);

    useEffect(() => {
        loadStatus();
        const id = setInterval(loadStatus, 60000); //segarkan tiap menit. kek tiap check in ditutup
        return () => clearInterval(id);
    }, [loadStatus]);

    const office = useMemo(
        () => (status ? { lat: status.officeLatitude, lng: status.officeLongitude } : null),
        [status]
    );
    if (!status) {
        return <p className="text-sm text-muted-foreground">{loadError || "Memuat status absesnsi..."}</p>;
    }

    const distance = position ? distanceMeters(position.lat, position.lng, office.lat, office.lng) : null;
    const inside = distance !== null && distance <= status.radiusMeters;
    const record = status.today;
    const ready = position && !submitting;

    let reason = "";
    if (geoError) reason = geoError;
    else if (!position) reason = "Mencari lokasi...";
    else if (!inside) reason = `Kamu berada sekitar ${Math.round(distance)} m dari kantor (radius ${status.radiusMeters} m)`;
    else if (status.hint) reason = status.hint;

    const act = async (type) => {
        setSubmitting(true);
        setMessage(null);
        try{
            const res = await fetch(`${API}/${type}`, {
                method: "POST",
                headers: { ...authHeaders, "Content-Type": "application/json" },
                body: JSON.stringify({ latitude: position.lat, longitude: position.lng, accuracy: position.accuracy }),
            });
            if (!res.ok) {
                if (res.status === 401) {
                    navigate("/login");
                    return;
                }
                setMessage({ type: "error", text: (await res.text()) || "Gagal mencatat absensi" });
                return;
            }
            const data = await res.json();
            setMessage({
                type: "ok",
                text: type === "check-in"
                    ? `Check-in tercatat pukul ${hhmm(data.checkInAt)}${data.checkInStatus === "LATE" ? "terlambat" : "tepat waktu"})`
                    : `Check-out tercatat pukul ${hhmm(data.checkOutAt)}. Total ${formatDuration(data.workMinutes)}`,
            });
            loadStatus();
        } catch {
            setMessage({ type: "error", text: "Tidak bisa terhubung ke server" });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Absensi</CardTitle>
                <CardDescription>
                    {status.workDate} &middot; <LiveClock offsetMs={offsetMs} /> WIB
                </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
                <AttendanceMap office={office} radius={status.radiusMeters} user={position} inside={inside} />

                <p className="text-xs text-muted-foreground">
                    {position
                        ? `Jarak ke kantor sekitar ${Math.round(distance)} m, akurasi lokasi sekitar ${Math.round(position.accuracy)} m`
                        : "Menunggu lokasi dari browser..."}
                </p>

                <div className="flex flex-col gap-2">
                    <Row label="Jam masuk resmi">
                        {status.workday ? `${status.officialStart} (tepat waktu s/d ${status.lateAfter})` : "Bukan hari kerja"}
                    </Row>
                    <Row label="Jam kerja dihitung mulai">{status.workStartTime} (atau jam check-in, jika lebih siang)</Row>
                    <Row label="Check-in">
                        {hhmm(record?.checkInAt)}{" "}
                        {record && (
                            <Badge variant={record.checkInStatus === "LATE" ? "destructive" : "secondary"}>
                                {record.checkInStatus === "LATE" ? "Terlambat" : "Tepat waktu"}
                            </Badge>
                        )}
                    </Row>
                    <Row label="Check-out terakhir">{hhmm(record?.checkOutAt)}</Row>
                    <Row label="Total jam kerja hari ini"><WorkedTime record={record} offsetMs={offsetMs} /></Row>
                </div>

                <div className="flex gap-3">
                    <Button className="flex-1" disabled={!ready || !status.canCheckIn || !inside} onClick={() => act("check-in")}>
                        Check In
                    </Button>
                    <Button className="flex-1" variant="outline" disabled={!ready || !status.canCheckOut || !inside} onClick={() => act("check-out")}>
                        {record?.checkOutAt ? "Perbarui Check Out" : "Check Out"}
                    </Button>
                </div>

                {reason && <p className="text-sm text-muted-foreground">{reason}</p>}
                {message && (
                    <p className={message.type === "ok" ? "text-sm text-green-600" : "text-sm text-destructive"}>{message.text}</p>
                )}
                {loadError && <p className="text-sm text-destructive">{loadError}</p>}
            </CardContent>
        </Card>
    );
}

export default Attendance;