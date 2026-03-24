import { useEffect, useState } from "react";
import "./App.css";

type Incident = {
  id: number;
  title: string;
  description: string;
  status: string;
  priority: string;
  source?: string | null;
};

type Alarm = {
  id: number;
  source: string;
  message: string;
  severity: string;
  createdAt: string;
};

const STATUS = ["OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"];
const PRIORITY = ["HIGH", "MEDIUM", "LOW"];
const SEVERITY = ["CRITICAL", "MAJOR", "MINOR"];

export default function App() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [alarms, setAlarms] = useState<Alarm[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [newIncident, setNewIncident] = useState({
    title: "",
    description: "",
    status: "OPEN",
    priority: "MEDIUM",
  });

  const [newAlarm, setNewAlarm] = useState({
    source: "router-12",
    message: "Link down",
    severity: "CRITICAL",
  });

  async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(path, {
      headers: { "Content-Type": "application/json" },
      ...init,
    });

    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ""}`);
    }

    const contentType = res.headers.get("content-type") ?? "";
    if (!contentType.includes("application/json")) return undefined as T;
    return (await res.json()) as T;
  }

  async function loadAll() {
    setLoading(true);
    setError(null);
    try {
      const [i, a] = await Promise.all([
        api<Incident[]>("/api/incidents"),
        api<Alarm[]>("/api/alarms"),
      ]);
      setIncidents(i);
      setAlarms(a);
    } catch (e: any) {
      setError(e?.message ?? "Unbekannter Fehler");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadAll();
  }, []);

  async function createIncident() {
    setError(null);
    try {
      await api<Incident>("/api/incidents", {
        method: "POST",
        body: JSON.stringify(newIncident),
      });
      setNewIncident({
        title: "",
        description: "",
        status: "OPEN",
        priority: "MEDIUM",
      });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Erstellen");
    }
  }

  async function updateStatus(id: number, status: string) {
    setError(null);
    try {
      await api<Incident>(`/api/incidents/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify(status),
      });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Status-Update");
    }
  }

  async function updatePriority(id: number, priority: string) {
    setError(null);
    try {
      await api<Incident>(`/api/incidents/${id}/priority`, {
        method: "PATCH",
        body: JSON.stringify(priority),
      });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Priority-Update");
    }
  }

  async function deleteIncident(id: number) {
    setError(null);
    try {
      await api<void>(`/api/incidents/${id}`, { method: "DELETE" });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Löschen");
    }
  }

  async function sendAlarm() {
    setError(null);
    try {
      await api<Incident>("/api/alarms", {
        method: "POST",
        body: JSON.stringify(newAlarm),
      });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Alarm senden");
    }
  }

  async function deleteAlarm(id: number) {
    setError(null);
    try {
      await api<void>(`/api/alarms/${id}`, { method: "DELETE" });
      await loadAll();
    } catch (e: any) {
      setError(e?.message ?? "Fehler beim Alarm löschen");
    }
  }

  return (
    <div
      style={{
        minHeight: "100vh",
        padding: "32px 16px",
        display: "flex",
        justifyContent: "center",
      }}
    >
      <div
        style={{
          width: "100%",
          maxWidth: 1100,
          backgroundColor: "white",
          borderRadius: 16,
          boxShadow: "0 4px 18px rgba(0, 0, 0, 0.08)",
          padding: 24,
        }}
      >
        <h1 style={{ marginTop: 0 }}>Incident Management MVP</h1>

        <div style={{ display: "flex", gap: 16, alignItems: "center", marginBottom: 16 }}>
          <button onClick={loadAll} disabled={loading}>
            {loading ? "Lade..." : "Neu laden"}
          </button>
          {error && <div style={{ color: "crimson" }}>{error}</div>}
        </div>

        <hr style={{ margin: "16px 0" }} />

        <h2>Alarm senden</h2>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 16 }}>
          <input
            value={newAlarm.source}
            onChange={(e) => setNewAlarm((s) => ({ ...s, source: e.target.value }))}
            placeholder="source"
          />
          <input
            value={newAlarm.message}
            onChange={(e) => setNewAlarm((s) => ({ ...s, message: e.target.value }))}
            placeholder="message"
          />
          <select
            value={newAlarm.severity}
            onChange={(e) => setNewAlarm((s) => ({ ...s, severity: e.target.value }))}
          >
            {SEVERITY.map((v) => (
              <option key={v} value={v}>
                {v}
              </option>
            ))}
          </select>
          <button onClick={sendAlarm}>Alarm senden</button>
        </div>

        <h3>Alarme</h3>
        <table width="100%" cellPadding={6} style={{ borderCollapse: "collapse", marginBottom: 24 }}>
          <thead>
            <tr style={{ textAlign: "left", borderBottom: "1px solid #ddd" }}>
              <th>ID</th>
              <th>Source</th>
              <th>Severity</th>
              <th>Message</th>
              <th>Created</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {alarms.map((a) => (
              <tr key={a.id} style={{ borderBottom: "1px solid #f0f0f0" }}>
                <td>{a.id}</td>
                <td>{a.source}</td>
                <td>{a.severity}</td>
                <td>{a.message}</td>
                <td>{new Date(a.createdAt).toLocaleString()}</td>
                <td>
                  <button onClick={() => deleteAlarm(a.id)}>Löschen</button>
                </td>
              </tr>
            ))}
            {alarms.length === 0 && (
              <tr>
                <td colSpan={6} style={{ opacity: 0.7 }}>
                  Keine Alarme
                </td>
              </tr>
            )}
          </tbody>
        </table>

        <hr style={{ margin: "16px 0" }} />

        <h2>Incident erstellen</h2>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 16 }}>
          <input
            value={newIncident.title}
            onChange={(e) => setNewIncident((s) => ({ ...s, title: e.target.value }))}
            placeholder="title"
          />
          <input
            value={newIncident.description}
            onChange={(e) => setNewIncident((s) => ({ ...s, description: e.target.value }))}
            placeholder="description"
          />
          <select
            value={newIncident.status}
            onChange={(e) => setNewIncident((s) => ({ ...s, status: e.target.value }))}
          >
            {STATUS.map((v) => (
              <option key={v} value={v}>
                {v}
              </option>
            ))}
          </select>
          <select
            value={newIncident.priority}
            onChange={(e) => setNewIncident((s) => ({ ...s, priority: e.target.value }))}
          >
            {PRIORITY.map((v) => (
              <option key={v} value={v}>
                {v}
              </option>
            ))}
          </select>
          <button onClick={createIncident}>Anlegen</button>
        </div>

        <h2>Incidents</h2>
        <table width="100%" cellPadding={6} style={{ borderCollapse: "collapse" }}>
          <thead>
            <tr style={{ textAlign: "left", borderBottom: "1px solid #ddd" }}>
              <th>ID</th>
              <th>Title</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Description</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {incidents.map((i) => (
              <tr key={i.id} style={{ borderBottom: "1px solid #f0f0f0" }}>
                <td>{i.id}</td>
                <td>{i.title}</td>
                <td>
                  <select value={i.status} onChange={(e) => updateStatus(i.id, e.target.value)}>
                    {STATUS.map((v) => (
                      <option key={v} value={v}>
                        {v}
                      </option>
                    ))}
                  </select>
                </td>
                <td>
                  <select value={i.priority} onChange={(e) => updatePriority(i.id, e.target.value)}>
                    {PRIORITY.map((v) => (
                      <option key={v} value={v}>
                        {v}
                      </option>
                    ))}
                  </select>
                </td>
                <td>{i.description}</td>
                <td>
                  <button onClick={() => deleteIncident(i.id)}>Löschen</button>
                </td>
              </tr>
            ))}
            {incidents.length === 0 && (
              <tr>
                <td colSpan={6} style={{ opacity: 0.7 }}>
                  Keine Incidents
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}