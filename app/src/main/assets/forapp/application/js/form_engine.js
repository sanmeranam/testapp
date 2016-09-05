var FormEngine = {
    init: function () {
        this.app = angular.module('FormApp', ['ngSanitize']);
    },
    defineController: function (name, fn) {
        this.app.controller(name, fn);
    },
    isMobile: function () {
        if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|ipad|iris|kindle|Android|Silk|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(navigator.userAgent)
                || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(navigator.userAgent.substr(0, 4)))
            return true;

        return false;
    }
};

FormEngine.init();


FormEngine.defineController('RootController', function ($scope) {
    $scope.model = null;
    $scope.form_meta = null;
    $scope.currentPage = 0;
    $scope.text = "section.html";
    
//    jQuery.getJSON("sample.json",function(data){
//        $scope.form_meta = data;
//        $scope.model = data.model_view;
//        $scope.aPages = Object.keys(data.model_view);
//        $scope.currentPage = 0;
//        $scope.$apply();
//    });

    jQuery.get("http://api.cloud4form.com/san/rest/form_meta", function (data) {
        $scope.form_meta = data[0];
        $scope.model = data[0].model_view;
        $scope.aPages = Object.keys(data[0].model_view);
        $scope.currentPage = 0;
        $scope.$apply();
    });

    $scope.switchPage = function (iPageIndex) {
        $scope.currentPage = iPageIndex;
    };

    $scope.navPage = function (idx) {
        $scope.currentPage = $scope.currentPage + idx;
        if ($scope.currentPage < 0)
            $scope.currentPage = 0;
        if ($scope.currentPage > $scope.aPages.length - 1) {
            $scope.currentPage = $scope.aPages.length - 1;
        }
        $scope.initPluginBind();
    };

    $scope.getPageModel = function () {
        if ($scope.model && $scope.aPages)
            return $scope.model[$scope.aPages[$scope.currentPage]]._c;
        $scope.initPluginBind();
        return null;
    };
    
    

    $scope.DesignerConfig = {
        clsSelection: function (item) {
            if (item === this.selected) {
                return (item._l ? 'selected_red' : 'selected');
            }
            return '';
        },
        clsSectionLayout: function (oBase, oItem) {
            if (oItem && oItem._a.space && oItem._a.space.value === "Full") {
                return "col-sm-12";
            }
            return "col-sm-" + (12 / oBase._a.columns.value);
        },
        getSectionClass: function (base) {
            switch (base.theme.value) {
                case "GRAY":
                    return "box-default";
                case "GRAY-SOLID":
                    return "box-default box-solid";
                case "GREEN":
                    return "box-success";
                case "GREEN-SOLID":
                    return "box-solid box-success";
                case "BLUE":
                    return "box-info";
                case "BLUE-SOLID":
                    return "box-solid box-info";
                case "ORANGE":
                    return "box-warning";
                case "ORANGE-SOLID":
                    return "box-solid box-warning";
                case "RED":
                    return "box-danger";
                case "RED-SOLID":
                    return "box-solid box-danger";
                default:
                    return "";
            }
        },
        getRateCal: function (max, val) {
            var m = [];
            m.length = max;
            m.fill(1);
            var u = Math.floor(val);
            m.fill(0, u);
            if ((Math.round(val) > Math.floor(val))) {
                m.fill(2, u);
                m.fill(0, u + 1);
            }
            return m;
        },
        clearInlineSign:function(item){
            $scope.Confirm.show(function(v){
                if(v){
                    item.value.value="";
                    item.name_value.value="";
                }
            });
        }
    };

//    Dropzone.options.myAwesomeDropzone = {
//        paramName: "file", // The name that will be used to transfer the file
//        maxFilesize: 2, // MB
//        accept: function (file, done) {
//            if (file.name == "justinbieber.jpg") {
//                done("Naha, you don't.");
//            } else {
//                done();
//            }
//        }
//    };

    $scope.Confirm = {
        show: function (callback) {
            this.callback = callback;
            jQuery("#ui_dialog_confirm").modal("show");
        },
        yes: function () {
            this.callback(true);
        },
        no: function () {
            this.callback(false);
        }
    };

    $scope.SignInputConfig = {
        init: function (canvas) {
            if (this.sign) {
                return;
            }
            this.canvas = canvas;
            jQuery(canvas).attr("width", window.innerWidth * 0.9);
            jQuery(canvas).attr("height", window.innerHeight * 0.6);
            this.sign = new SignaturePad(canvas);
        },
        show: function (oData) {
            this.source = oData;
            var mData = angular.copy(oData);
            this.sign.fromDataURL(mData.value.value);
            this.name = mData.name_value.value;
            this.error = "";
            jQuery("#ui_modal_sign_pad2").modal("show");
        },
        _ok: function () {
            if (this.sign.isEmpty() || !this.name) {
                this.error = "Signature or name can't be empty.";
            } else {
                this.source.value.value = this.sign.toDataURL("image/png");
                this.source.name_value.value = this.name;
                jQuery("#ui_modal_sign_pad2").modal("hide");
            }

        },
        _cancel: function () {

        },
        _clear: function () {
            this.sign.clear();
        }
    };

    $scope._delayDZ = {};

    $scope.initPluginBind = function () {
        $scope._delayDZ[$scope.currentPage] = setInterval(function () {
            if (jQuery("#test_id").length) {

                $scope.SignInputConfig.init(jQuery("#modal_sign_pad").get(0));


//            jQuery(".dropzone").dropzone({url: "/file/post"});
                $('.datepicker').pickdate({
                    cancel: 'Clear',
                    closeOnCancel: false,
                    closeOnSelect: true,
                    container: '',
                    firstDay: 1,
                    format: 'dddd, d mm, yy', // escape any formatting characters with an exclamation mark
                    formatSubmit: 'dd/mmmm/yyyy',
                    ok: 'Close',
                    onClose: function () {
//                    $('body').snackbar({
//                        content: 'Datepicker closes'
//                    });
                    },
                    onOpen: function () {
//                    $('body').snackbar({
//                        content: 'Datepicker opens'
//                    });
                    },
                    selectMonths: true,
                    selectYears: 10,
                    today: ''
                });
                if (FormEngine.isMobile()) {
                    $(".timepicker").attr("type", "time");
                } else {
                }
                $.each($('.starrr'), function () {
                    $(this).starrr({
                        rating: $(this).attr("val"),
                        max: $(this).attr("stars"),
                        change: function () {

                        }
                    });
                });

                $("[data-mask]").inputmask();

                clearInterval($scope._delayDZ);
            }
        }, 200);
    };
    $scope.initPluginBind();
});