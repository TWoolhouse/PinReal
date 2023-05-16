<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
	<header>
		<RouterLink to="/">🏠</RouterLink>
		<RouterLink to="proximity">🎯</RouterLink>
		<RouterLink to="landmark">🗼</RouterLink>
		<RouterLink to="user">👤</RouterLink>
		<RouterLink to="post">📷</RouterLink>
	</header>
	<main>
		<RouterView />
	</main>
</template>

<style scoped>
header {
	font-size: 2em;
	/* padding: 1rem 0; */
	margin: 0 auto;
	width: fit-content;
	display: flex;
	flex-wrap: wrap;
	flex-direction: row;
	justify-content: center;
	top: 0;
	position: sticky;
	background-color: var(--colour-background);
	border-bottom: 2px solid var(--colour-text);
	max-width: var(--max-width);
}

a {
	text-decoration: none;
	margin: 0.5rem 0.25rem;
	padding: 0.3rem;
	padding-bottom: 0.6rem;
	border-radius: 1rem;
}

.router-link-active {
	color: var(--colour-background);
	background-color: var(--colour-text);
}

main {
	max-width: var(--max-width);
	margin: 0 auto;
}
</style>
