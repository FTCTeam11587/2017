color ("DarkBlue",1.0) {
    cube (size=[200,77,8],center=false);
}
color ("Silver",1.0) {
    translate([3,8,6])
    linear_extrude (height = 8, center = false) {
        text("11587", font="Ubuntu Condensed", size=64);
    }
}