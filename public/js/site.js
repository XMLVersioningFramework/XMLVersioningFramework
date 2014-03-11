
getUserStories();
function getUserStories(){
	$.ajax({
	  dataType: "json",
	  url: "/getUserstories",
	  success: success,
	  error: error
	});
	function success(data){
		
		$(data).each(function(nr,story){
			console.log(story);
			$("#listUserStories").append(story.name+"<br />");
		});
		//$("#listUserStorys").html(data);
		
	}
	function error(data){
		alert("error");
		console.log(data);
	}
}

function getaUserStory(story){
	$.ajax({
	  dataType: "json",
	  url: "/getAUserstory/"+story,
	  success: success,
	  error: error
	});
	function success(data){
		console.log(data);
		
		
	}
	function error(data){
		alert("error");
		console.log(data);
	}
}