<template>
	<h1>User</h1>
	<p>The user page allows you to see every post this person has made, (including your own). It shows some extra information as well as this, including:
	<ul>
		<li>Their username.</li>
		<li>Profile picture.</li>
		<li>The number of landmarks they have visited.</li>
	</ul>
	The posts at the bottom of the page will link to either the <RouterLink to="post">Post 📷</RouterLink> or the <RouterLink to="landmark">Landmark 🗼</RouterLink> of that post.
	</p>
	<h2>Following</h2>
	<img src="@/assets/follow.png" alt="Following">
	<p>On the user page, you are also able to follow and unfollow other people.
		This means you will receive a notification if they make a new post by reaching a landmark.</p>
</template>
