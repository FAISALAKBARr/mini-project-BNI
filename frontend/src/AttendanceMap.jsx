import {CircleMarker, MapContainer, TileLayer, useMap} from "react-leaflet";
import {useEffect, useRef} from "react";
import "leaflet/dist/leaflet.css"

// Menyesuaikan tampilan peta sekali saja, agar kantor dan posisi pegawai sama-sama terlihat.
function FitOnce({ office, user }) {
    const map = useMap();
    const done = useRef(false);

    useEffect(() => {
        if (user && !done.current) {
            done.current = true;
            map.fitBounds(
                [[office.lat, office.lng], [user.lat, user.lng]],
                { padding: [40, 40], maxZoom: 18 }
            );
        }
    }, [map, office, user]);

    return null;
}

export default function AttendanceMap({ office, radius, user, inside }) {
    return (
        <MapContainer center={[office.lat, office.lng]} zoom={17} className="h-80 w-full rounded-lg">
            <TileLayer
                url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />
            {/* lingkaran radius kantor + titik pusat kantor */}
            <Circle center={[office.lat, office.lng]} radius={radius} pathOptions={{ color: "#16a34a", fillOpacity: 0.1 }} />
            <CircleMarker center={[office.lat, office.lng]} radius={6} pathOptions={{ color: "#16a34a" }} />
            {/* posisi pegawai: biru kalau di dalam radius, merah kalau di luar; lingkaran tipis = akurasi GPS */}
            {user && (
                <>
                    <Circle center={[user.lat, user.lng]} radius={user.accuracy} pathOptions={{ color: "#2563eb", weight: 1, fillOpacity: 0.08 }} />
                    <CircleMarker center={[user.lat, user.lng]} radius={7} pathOptions={{ color: inside ? "#2563eb" : "#dc2626", fillOpacity: 0.9 }} />
                </>
            )}
            <FitOnce office={office} user={user} />
        </MapContainer>
    );
}