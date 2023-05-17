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

	<h2>Camera</h2>
	<img src="@/assets/camera.png" alt="Taking a Photo">
	<p>If you are within a set range of a landmark, you will be prompted to open the camera and take a picture of the landmark.
		If you choose to take a picture, your default camera app will be open and allow you to take a picture.
		Once this has completed, a new post on your account will be immediately created and a notification will be sent out to your followers.</p>
</template>
