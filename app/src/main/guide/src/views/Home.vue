<script setup lang="ts">

</script>

<template>
	<h1>User Guide</h1>
	<p>PinReal is a photo and adventure sharing app, centred around visiting and exploring landmarks.
	</p>
	<h2>Nearby</h2>
	<img src="@/assets/nearby.png" alt="Nearby Landmarks">
	<p>
		On the home screen, the first list shows the closest landmarks to your current location, sorted by nearest first.
		If you would like to see the exact location of a landmark, you may hit the location button and see it in your default maps app.
		To see more about a specific landmark, tapping on the name will show you more information about said <RouterLink to="landmark">Landmark 🗼</RouterLink>
	</p>
	<h2>Recent</h2>
	<img src="@/assets/following.png" alt="Recent Photos">
	<p>The second half of the page, consists of the most recent photos taken on the app. This list shows both the user and the landmark and a small version of the photo.
		To see more about either of these, tapping on the respective segment will lead you to the <RouterLink to="user">User 👤</RouterLink>, <RouterLink to="landmark">Landmark 🗼</RouterLink>, or the <RouterLink to="post">Post 📷</RouterLink> pages.
	</p>
</template>

<style scoped lang="scss">
</style>
