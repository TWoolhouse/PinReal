<template>
	<h1>Landmark</h1>
	<p>Details and posts of a single landmark can be seen here.
		More specifically, you are able to see:
	<ul>
		<li>The name.</li>
		<li>A brief description.</li>
		<li>A button to take you to the landmark on your default maps app.</li>
		<li>Your current distance to the landmark.</li>
		<li>The number of people who have reached this landmark already.</li>
		<li>A default image of the landmark.</li>
	</ul>
	Futhermore, a list of all posts at this landmark can be seen at the bottom. You may tap on either the images or the name and they will take you to the <RouterLink to="/post">Post 📷</RouterLink> or the <RouterLink to="user">User 👤</RouterLink> respectively.
	</p>
</template>
