import { useEffect, useState } from "react";

// Memantau posisi pegawai lewat Geolocation API milik browser.
export function useGeolocation() {
    const [position, setPosition] = useState(null); // { lat, lng, accuracy }
    const [error, setError] = useState("");

    useEffect(() => {
        if (!navigator.geolocation) {
            setError("Browser ini tidak mendukung lokasi");
            return;
        }
        const watchId = navigator.geolocation.watchPosition(
            (pos) => {
                setError("");
                setPosition({
                    lat: pos.coords.latitude,
                    lng: pos.coords.longitude,
                    accuracy: pos.coords.accuracy,
                });
            },
            (err) => {
                if (err.code === 1) setError("Izin lokasi ditolak. Izinkan akses lokasi di browser, lalu muat ulang halaman.");
                else if (err.code === 2) setError("Lokasi tidak tersedia. Aktifkan GPS atau Wi-Fi.");
                else setError("Gagal mendapatkan lokasi (timeout), mencoba lagi...");
            },
            { enableHighAccuracy: true, maximumAge: 10000, timeout: 20000 }
        );
        // cleanup: dijalankan saat komponen unmount, supaya pemantauan lokasi berhenti
        return () => navigator.geolocation.clearWatch(watchId);
    }, []);

    return { position, error };
}