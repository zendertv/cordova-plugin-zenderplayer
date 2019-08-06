exports.defineAutoTests = function() {
  describe('cordova-plugin-zenderplayer', function() {

    it('is mounted', function() {
      expect(window.zender.player).toBeDefined();
    });


  });
};
