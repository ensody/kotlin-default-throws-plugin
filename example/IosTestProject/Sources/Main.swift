import Foundation
import UIKit
import Shared

@main
class AppDelegate: UIResponder, UIApplicationDelegate {

  var window: UIWindow?
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    // Override point for customization after application launch.
    return true
  }
}

public extension Error {
    var kotlinException: KotlinThrowable? {
        (self as NSError).userInfo["KotlinException"] as? KotlinThrowable
    }
}
