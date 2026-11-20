import { useEffect, useState } from "react";

// Waktu "sekarang" menurut server = jam browser + selisih (offset) yang dihitung dari serverTime.
export function useNow(offsetMs, intervalMs = 1000) {
    const [now, setNow] = useState(() => Date.now() + offsetMs);

    useEffect(() => {
        const tick = () => setNow(Date.now() + offsetMs);
        tick();
        const id = setInterval(tick, intervalMs);
        return () => clearInterval(id);
    }, [offsetMs, intervalMs]);

    return now;
}