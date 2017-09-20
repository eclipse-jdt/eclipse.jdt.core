module aaaaaaaaaa.bbbbbbbbbb{
  requires aaaaaaaaaa.cccccccccc; // a      comment
  requires transitive aaaaaaaaaa
      .dddddddddd;
  requires static aaaaaaaaaa.eeeeeeeeee;
  requires transitive static aaaaaaaaaa.ffffffffff.ggggggggg.hhhhhhhhhh.iiiiiiiiii;
  exports aaaaaaaaaa.jjjjjjjjjj;
  exports aaaaaaaaaa.kkkkkkkkkk to aaaaaaaaaa.llllllllll, aaaaaaaaaa.mmmmmmmmmm, aaaaaaaaaa.nnnnnnnnnn;
  opens aaaaaaaaaa.oooooooooo;
  opens aaaaaaaaaa.pppppppppp to aaaaaaaaaa.qqqqqqqqqq;
  uses aaaaaaaaaa.ssssssssss;
  provides aaaaaaaaaa.tttttttttt with aaaaaaaaaa.uuuuuuuuuu, aaaaaaaaaa.vvvvvvvvvv;
}