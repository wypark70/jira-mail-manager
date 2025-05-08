// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
	namespace App {
		// interface Error {}
		// interface Locals {}
		// interface PageData {}
		// interface PageState {}
		// interface Platform {}
	}
	interface Mail {
		id: number;
		recipient: string;
		subject: string;
		body: string;
		createdAt: string; // 또는 Date
		sentAt: string | null; // 또는 Date | null
	}
}

export {};
