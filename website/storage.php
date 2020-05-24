<?php 
require_once("connect.php");
session_start();
include ("navbar.html");

$result = "";
$namacuy = $_SESSION['email'];

if(isset($_SESSION['email'])){
    $hiddentag = 0;
    // buat upload video
    if($_POST['hiddentag'] == 1 ){
        $target_dir = "uploads/videos/";
        $target_file = $target_dir.basename($_FILES["videofile"]["name"]);
        $fileType = pathinfo($target_file,PATHINFO_EXTENSION);
        
        if($fileType != "mp4" && $fileType != "avi" && $fileType != "mov" && $fileType != "mpeg"){
            $result = 
            "<div class='alert alert-dismissible alert-danger'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                Upload only <strong>mp4, avi, mov</strong> and <strong> mpeg </strong> !
            </div>";
        }else{
            $whose = $_SESSION['email'];
            $videoPath = $_FILES['videofile']['name'];
            $video_name = $_POST['video_name'];
            $startloc = $_POST['start_location'];
            $endloc = $_POST['end_location'];
            $datetaken = $_POST['date_taken'];
            // komen dlu biar ga refresh abis tu masuk lagi
            $sql = "INSERT INTO videos (whose, thumbnail,video_name,date_taken,start_location,end_location) VALUES ('$whose','$videoPath','$video_name','$datetaken','$startloc','$endloc');";
            // $q = mysqli_query($conn,$sql);
            move_uploaded_file($_FILES['videofile']['tmp_name'],$target_file);
            if($q == TRUE){
                $result = 
                    "<div class='alert alert-dismissible alert-success'>
                        <button type='button' class='close' data-dismiss='alert'>&times;</button>
                        <strong>Uploaded successfully!</strong>
                    </div>";
            }else{
                $result = 
                "<div class='alert alert-dismissible alert-danger'>
                    <button type='button' class='close' data-dismiss='alert'>&times;</button>
                    <strong>Upload failed!</strong>
                </div>";
            }
        }
    }else if($_POST['hiddentag'] == 2){
        $target_dir = 'uploads/photos/';
        $target_file = $target_dir . basename($_FILES['photofile']['name']);
        $fileType = pathinfo($target_file,PATHINFO_EXTENSION);
        if($fileType != 'jpg' && $fileType != 'png'){
            $result = 
            "<div class='alert alert-dismissible alert-danger'>
                <button type='button' class='close' data-dismiss='alert'>&times;</button>
                Upload only <strong>jpg, and <strong> png </strong> !
            </div>";
        }else{
            $whosep = $_SESSION['email'];
            $photo = $_FILES['photofile']['name'];
            $photoloc = $_POST['photolocation'];
            $descriptionp = $_POST['description'];
            $date_photo = $_POST['date_photo'];
            $sql = "INSERT INTO photos (whose, thumbnail, location, description,date_taken) values ('$whosep','$photo','$photoloc','$descriptionp','$date_photo');";
            // $q = mysqli_query($conn,$sql);
            move_uploaded_file($_FILES['photofile']['tmp_name'],$target_file);
            // var_dump($_SESSION);
            if($q == TRUE){
                $result = 
                    "<div class='alert alert-dismissible alert-success'>
                        <button type='button' class='close' data-dismiss='alert'>&times;</button>
                        <strong>Uploaded successfully!</strong>
                    </div>";
            }else{
                $result = 
                "<div class='alert alert-dismissible alert-danger'>
                    <button type='button' class='close' data-dismiss='alert'>&times;</button>
                    <strong>Upload failed!</strong>
                </div>";
            }
        }
    }else{
        $result="";
    }
}else{
    header('Location: login.php');
}
?>
<!DOCTYPE html>
<html>
    
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <head>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
        <link rel="stylesheet" href="bootstrap/bootstrap.css">
        <link rel="stylesheet" href="forodal.css">
        
        <style>
            .container{
                padding-top:3%;
            }
        </style>
    </head>
    <body>
        <?php echo $result; ?>
        <div class="container">
        <h1>Storage</h1>
            <ul class="nav nav-tabs">
                <li class="nav-item"><a class = "nav-link active" href="#videos" data-toggle="tab">Videos</a></li>
                <li class="nav-item"><a class = "nav-link" href="#photos" data-toggle="tab">Photos</a></li>
            </ul>
            <div class="tab-content">
                <div class="tab-pane active" id="videos">
            <!-- Isi Konten -->
                    <div class="table-responsive">
                        <table class="table table-hover">   
                            <tr class="table-light">    
                                <th>Thumbnail</th>  
                                <th>Video Name</th> 
                                <th>Date Taken</th> 
                                <th>Start Location</th> 
                                <th>Location End</th>   
                                <th>Action</th>
                            </tr>   
            <!-- Selesai konten -->
                            <!-- value table -->  
                            <?php
                                $sqlv = "SELECT * FROM videos where whose = '$namacuy'";
                                $qv = mysqli_query($conn,$sqlv);
                                while($datavideo = mysqli_fetch_assoc($qv)){
                                    $idv = $datavideo['id'];
                                    $thumbnails = $datavideo["thumbnail"];
                                    $videoname = $datavideo["video_name"];
                                    $startloca = $datavideo["start_location"];
                                    $endloca = $datavideo["end_location"];
                                    $date_taken = $datavideo["date_taken"];
                                    echo "<tr>";
                                    echo "<th>
                                            <video width='300' height='200' controls>
                                                <source src='uploads/videos/$thumbnails' type='video/mp4'>
                                            </video>
                                        </th>";
                                    echo "<th>$videoname</th>";
                                    echo "<th>$startloca</th>";
                                    echo "<th>$endloca</th>";
                                    echo "<th>$date_taken</th>";
                                    echo "<th>
                                            <a href='delete_item.php?id=$idv&subject=video'><img src='assets/bin.png' width='25' height='25'></a>
                                            <a href='update_item.php?id=$idv&subject=video'><img src='assets/edit.png' width='25' height='25'></a>
                                        </th>";
                                    echo "</tr>";
                                }
                            ?>
                        </table>
                    </div>
                    <div class="form-group">
                        <form method="post" action="" enctype="multipart/form-data">
                            <br><br><hr>
                            <label>Upload a new video file</label>
                                <input type="text" name="video_name" class="form-control" placeholder="Video name"><br>
                                <input type="text" name="start_location" class="form-control" placeholder="Start Location"><br>
                                <input type="text" name="end_location" class="form-control" placeholder="Location End"><br>
                                <input type="hidden" name="hiddentag" value="1"> 
                                <label>Date taken: </label><br>
                                <input type="date" name="date_taken" class="form-control-date"><br><br>
                                <input type="file" class="form-control-file" name="videofile"><br>
                                <input type="submit" value="Upload" class="btn btn-primary">
                        </form>
                    </div>
                </div>
                <div class="tab-pane" id="photos">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <tr class="table-light">
                                <th>Thumbnail</th>
                                <th>Photo Location</th>
                                <th>Description</th>
                                <th>Photo taken</th>
                                <th>Action</th>
                            </tr>
                            <!-- value table -->
                            <?php
                            $sqlp = "SELECT * FROM photos where whose = '$namacuy'";
                            $qp = mysqli_query($conn,$sqlp);
                            while($dataphoto = mysqli_fetch_assoc($qp)){
                                $idp = $dataphoto['id'];
                                $thumbnailp = $dataphoto['thumbnail'];
                                $locationp  = $dataphoto['location'];
                                $description = $dataphoto['description'];
                                $datetakenp = $dataphoto['date_taken'];
                                
                                echo "<tr>";
                                echo "<th>
                                        <img alt='$description' src='uploads/photos/$thumbnailp' class='img-thumbnail' width='200' height='200'>
                                        <div id='modalImg' class='modal'>
                                            <span class='close'>&times</span>
                                            <img class='modal-content' id='imgm'>
                                            <div id='caption'></div>
                                        </div>
                                      </th>";
                                echo "<th>$locationp</th>";
                                echo "<th>$description</th>";
                                echo "<th>$datetakenp</th>";
                                echo "<th>
                                            <a href='delete_item.php?id=$idp&subject=photo'><img src='assets/bin.png' width='30' height='30'></a>
                                            <a href='update_item.php?id=$idp&subject=photo'><img src='assets/edit.png' width='30' height='30'></a>
                                    </th>";
                            }
                            ?>
                        </table>
                        <hr>
                    </div>
                        <form method="post" action="" enctype='multipart/form-data'>
                        <label>Upload a new photo file</label>
                            <input type="text" name="photolocation" class="form-control" placeholder="Location photo taken"><br>
                            <input type="text" name="description" class="form-control" placeholder="Description"><br>
                            <label>Date taken:</label><br>
                            <input type="date" name="date_photo"><br>
                            <input type="hidden" name="hiddentag" value="2"> <br>
                            <input type="file" class="form-control-file" name="photofile"><br>
                            <input type="submit" value="Upload" class="btn btn-primary">
                        </form>
                </div>
            </div>
            <!-- end tab pane -->
        </div>
    </body>
    <script>
        var modal = document.getElementById('modalImg');

        var img = document.getElementsByClassName('img-thumbnail');
        var i = img.length;
        var modalImg = document.getElementById('imgm');
        var caption = document.getElementById('caption');
        for(var j = 0; j < i; j++){
            img[j].onclick = function(){
                modal.style.display='block';
                modalImg.src = this.src;
                caption.innerHTML = this.alt;
            }
        }
        var span = document.getElementsByClassName('close');

        for(var j = 0 ; j < span.length;j++){
            span[j].onclick = function(){
                modal.style.display='none';
            }
        }

        
    </script>
</html>